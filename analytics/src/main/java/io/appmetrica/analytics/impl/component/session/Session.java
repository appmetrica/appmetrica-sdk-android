package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static io.appmetrica.analytics.impl.component.session.SessionDefaults.MIN_VALID_UI_SESSION_ID;

public class Session {
    public static final String TAG = "[Session]";

    private final ComponentUnit component;
    private final SessionStorage sessionStorage;
    private final SessionArgumentsInternal sessionArguments;
    private long id;
    private long creationElapsedRealtime;
    private long creationCurrentTimeMillis;
    private AtomicLong currentReportId;
    private boolean aliveNeeded;
    private volatile SessionRequestParams sessionRequestParams;
    private long sleepStart;
    private long lastEventTimeOffset;
    private boolean crashedSession;

    private final SystemTimeProvider systemTimeProvider;

    Session(ComponentUnit component, SessionStorage storage, SessionArgumentsInternal arguments) {
        this(component, storage, arguments, new SystemTimeProvider());
    }

    Session(ComponentUnit component, SessionStorage storage, SessionArgumentsInternal arguments,
            SystemTimeProvider systemTimeProvider) {
        this.component = component;
        sessionStorage = storage;
        sessionArguments = arguments;
        this.systemTimeProvider = systemTimeProvider;
        initializeWithArguments();
    }

    private void initializeWithArguments() {
        creationElapsedRealtime = sessionArguments.getCreationElapsedRealTime(systemTimeProvider.elapsedRealtime());
        creationCurrentTimeMillis =
            sessionArguments.getCreationCurrentTimeMillis(systemTimeProvider.currentTimeMillis());
        id = sessionArguments.getId(SessionDefaults.INVALID_UI_SESSION_ID);
        currentReportId = new AtomicLong(sessionArguments.getCurrentReportId(SessionDefaults.INITIAL_REPORT_ID));
        aliveNeeded = sessionArguments.isAliveNeeded(true);
        sleepStart = sessionArguments.getSleepStart(SessionDefaults.INITIAL_SESSION_TIME);
        lastEventTimeOffset = sessionArguments.getLastEventOffset(sleepStart - creationElapsedRealtime);
        crashedSession = sessionArguments.isCrashedSession(false);
    }

    protected SessionType getType() {
        return sessionArguments.getType();
    }

    protected int getTimeoutSeconds() {
        return sessionArguments.getTimeout(component.getFreshReportRequestConfig().getSessionTimeout());
    }

    public long getId() {
        return id;
    }

    long getAliveReportOffsetSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(Math.max(sleepStart - creationElapsedRealtime, lastEventTimeOffset));
    }

    boolean isValid(long reportElapsedRealtime) {
        boolean validID = id >= MIN_VALID_UI_SESSION_ID;
        boolean consistentRequestParameters = consistentRequestParameters();
        boolean notExpired = !isExpired(reportElapsedRealtime, systemTimeProvider.elapsedRealtime());
        DebugLogger.INSTANCE.info(
            TAG,
            "Session id=%d and type %s validity. validID=%b, consistentRequestParameters=%b, notExpired=%b; " +
                "crashedSession=%s",
            id, sessionArguments.getType(), validID, consistentRequestParameters, notExpired, crashedSession
        );
        return validID && consistentRequestParameters && notExpired && !crashedSession;
    }

    private boolean consistentRequestParameters() {
        SessionRequestParams requestParams = component.getDbHelper().getSessionRequestParams(getId(), getType());
        boolean consistentRequestParameters = false;
        if (requestParams != null) {
            ReportRequestConfig reportRequestConfig = component.getFreshReportRequestConfig();
            consistentRequestParameters = requestParams.areParamsSameAsInConfig(reportRequestConfig);
        } else {
            DebugLogger.INSTANCE.info(
                TAG,
                "SessionRequestParameters are null. SessionID %d, SessionType %s",
                id,
                sessionArguments.getType()
            );
        }

        return consistentRequestParameters;
    }

    private long getSessionTimeOffset(long ellapsedRealtime) {
        return ellapsedRealtime - creationElapsedRealtime;
    }

    @VisibleForTesting
    boolean isExpired(long reportElapsedRealtime, long currentElapsedRealtime) {
        boolean wasRebooted = currentElapsedRealtime < sleepStart;
        final long sinceLastActive= reportElapsedRealtime - sleepStart;
        long sessionLength = getSessionTimeOffset(reportElapsedRealtime);
        DebugLogger.INSTANCE.info(
            TAG,
            "Session: id = %d, type = %s, sleepStart = %d, sinceLastActive = %d, sessionTimeout = %d",
            getId(),
            getType().toString(),
            sleepStart,
            sinceLastActive,
            getTimeoutSeconds()
        );
        return wasRebooted
            || sinceLastActive >= TimeUnit.SECONDS.toMillis(getTimeoutSeconds())
            || sessionLength >= TimeUnit.SECONDS.toMillis(SessionDefaults.SESSION_MAX_LENGTH_SEC);
    }

    synchronized void markSessionAsCrashed() {
        DebugLogger.INSTANCE.info(TAG, "markSessionAsCrashed: type = %s; id = %s", getType(), getId());
        crashedSession = true;
        sessionStorage.putCrashedSession(true).apply();
    }

    synchronized boolean isSessionCrashed() {
        return crashedSession;
    }

    synchronized void stopSession() {
        DebugLogger.INSTANCE.info(TAG, "stopSession: %s", id);
        sessionStorage.clear();
        sessionRequestParams = null;
    }

    void updateLastActiveTime(long elapsedRealtime) {
        sleepStart = elapsedRealtime;
        DebugLogger.INSTANCE.info(TAG, "updateLastActiveTime: %s", sleepStart);
        sessionStorage.putSleepStart(sleepStart).apply();
    }

    long getAndUpdateLastEventTimeSeconds(long elapsedRealtime) {
        sessionStorage.putLastEventOffset(lastEventTimeOffset = getSessionTimeOffset(elapsedRealtime)).apply();
        return TimeUnit.MILLISECONDS.toSeconds(lastEventTimeOffset);
    }

    // currentTimeMillis-based offset is preferred for prev session events because it accounts for device reboots
    // and clock adjustments — significant time may pass between crash capture and its delivery,
    // unlike regular events. The elapsedRealtime-based offset serves as a fallback for sessions
    // that were persisted without a creationCurrentTimeMillis value.
    long getEventTimeOffsetForPrevSession(long eventCurrentTimeMillis, long elapsedRealtime) {
        long timestampBasedOffset = TimeUnit.MILLISECONDS.toSeconds(eventCurrentTimeMillis - creationCurrentTimeMillis);
        long elapsedRealtimeBasedOffset = getAndUpdateLastEventTimeSeconds(elapsedRealtime);
        return Math.max(timestampBasedOffset, elapsedRealtimeBasedOffset);
    }

    long getLastEventTimeOffsetSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(lastEventTimeOffset);
    }

    long getNextReportId() {
        long reportId = currentReportId.getAndIncrement();
        sessionStorage.putReportId(currentReportId.get()).apply();
        return reportId;
    }

    boolean isAliveNeeded() {
        return aliveNeeded && getId() > 0;
    }

    public void updateAliveReportNeeded(final boolean value) {
        if (aliveNeeded != value) {
            aliveNeeded = value;
            sessionStorage.putAliveReportNeeded(aliveNeeded).apply();
        }
    }

    @Override
    @NonNull
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", creationTime=" + creationElapsedRealtime +
                ", sessionCreationCurrentTimeMillis=" + creationCurrentTimeMillis +
                ", currentReportId=" + currentReportId +
                ", sessionRequestParams=" + sessionRequestParams +
                ", sleepStart=" + sleepStart +
                ", aliveNeeded=" + aliveNeeded +
                ", crashedSession=" + crashedSession +
                '}';
    }

    public long getCreationCurrentTimeMillis() {
        return creationCurrentTimeMillis;
    }

    @VisibleForTesting
    public long getSleepStart() {
        return sleepStart;
    }
}
