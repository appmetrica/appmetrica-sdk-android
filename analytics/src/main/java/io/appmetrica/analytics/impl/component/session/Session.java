package io.appmetrica.analytics.impl.component.session;

import android.content.ContentValues;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONObject;

import static io.appmetrica.analytics.impl.component.session.SessionDefaults.MIN_VALID_UI_SESSION_ID;

public class Session {
    public static final String TAG = "[Session]";

    private final ComponentUnit component;
    private final SessionStorage sessionStorage;
    private final SessionArgumentsInternal sessionArguments;

    private long id;
    private long creationTime;
    private AtomicLong currentReportId;
    private boolean aliveNeeded;
    private volatile SessionRequestParams sessionRequestParams;
    private long sleepStart;
    private long lastEventTimeOffset;

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
        creationTime = sessionArguments.getCreationTime(systemTimeProvider.elapsedRealtime());
        id = sessionArguments.getId(SessionDefaults.INVALID_UI_SESSION_ID);
        currentReportId = new AtomicLong(sessionArguments.getCurrentReportId(SessionDefaults.INITIAL_REPORT_ID));
        aliveNeeded = sessionArguments.isAliveNeeded(true);
        sleepStart = sessionArguments.getSleepStart(SessionDefaults.INITIAL_SESSION_TIME);
        lastEventTimeOffset = sessionArguments.getLastEventOffset(sleepStart - creationTime);
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
        return TimeUnit.MILLISECONDS.toSeconds(Math.max(sleepStart - creationTime, lastEventTimeOffset));
    }

    boolean isValid(long reportElapsedRealtime) {
        boolean validID = id >= MIN_VALID_UI_SESSION_ID;
        boolean consistentRequestParameters = consistentRequestParameters();
        boolean notExpired = !isExpired(reportElapsedRealtime, systemTimeProvider.elapsedRealtime());
        YLogger.info(
            TAG,
            "Session id=%d and type %s validity. validID=%b, consistentRequestParameters=%b, notExpired=%b",
            id, sessionArguments.getType(), validID, consistentRequestParameters, notExpired
        );
        return validID && consistentRequestParameters && notExpired;
    }

    private boolean consistentRequestParameters() {
        SessionRequestParams requestParams = getSessionRequestParams();
        boolean consistentRequestParameters = false;
        if (requestParams != null) {
            ReportRequestConfig reportRequestConfig = component.getFreshReportRequestConfig();
            consistentRequestParameters = requestParams.areParamsSameAsInConfig(reportRequestConfig);
        } else {
            YLogger.info(
                TAG,
                "SessionRequestParameters are null. SessionID %d, SessionType %s",
                id,
                sessionArguments.getType()
            );
        }

        return consistentRequestParameters;
    }

    private long getSessionTimeOffset(long ellapsedRealtime) {
        return ellapsedRealtime - creationTime;
    }

    @VisibleForTesting
    boolean isExpired(long reportElapsedRealtime, long currentElapsedRealtime) {
        boolean wasRebooted = currentElapsedRealtime < sleepStart;
        final long sinceLastActive= reportElapsedRealtime - sleepStart;
        long sessionLength = getSessionTimeOffset(reportElapsedRealtime);
        if (YLogger.DEBUG) {
            YLogger.info(
                TAG,
                "Session: id = %d, type = %s, sleepStart = %d, sinceLastActive = %d, sessionTimeout = %d",
                getId(), getType().toString(), sleepStart, sinceLastActive, getTimeoutSeconds()
            );
        }
        return wasRebooted
            || sinceLastActive >= TimeUnit.SECONDS.toMillis(getTimeoutSeconds())
            || sessionLength >= TimeUnit.SECONDS.toMillis(SessionDefaults.SESSION_MAX_LENGTH_SEC);
    }

    synchronized void stopSession() {
        sessionStorage.clear();
        sessionRequestParams = null;
    }

    void updateLastActiveTime(long elapsedRealtime) {
        sleepStart = elapsedRealtime;
        YLogger.info(TAG, "updateLastActiveTime: %s", sleepStart);
        sessionStorage.putSleepStart(sleepStart).commit();
    }

    long getAndUpdateLastEventTimeSeconds(long elapsedRealtime) {
        sessionStorage.putLastEventOffset(lastEventTimeOffset = getSessionTimeOffset(elapsedRealtime));
        return TimeUnit.MILLISECONDS.toSeconds(lastEventTimeOffset);
    }

    long getLastEventTimeOffsetSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(lastEventTimeOffset);
    }

    long getNextReportId() {
        long reportId = currentReportId.getAndIncrement();
        sessionStorage.putReportId(currentReportId.get()).commit();
        return reportId;
    }

    boolean isAliveNeeded() {
        return aliveNeeded && getId() > 0;
    }

    public void updateAliveReportNeeded(final boolean value) {
        if (aliveNeeded != value) {
            aliveNeeded = value;
            sessionStorage.putAliveReportNeeded(aliveNeeded).commit();
        }
    }

    private SessionRequestParams getSessionRequestParams() {
        if (sessionRequestParams == null) {
            synchronized (this) {
                if (sessionRequestParams == null) {
                    try {
                        ContentValues params = component.getDbHelper().getSessionRequestParameters(getId(), getType());
                        final String paramsJson = params.getAsString(
                                Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS
                        );

                        if (!TextUtils.isEmpty(paramsJson)) {
                            JSONObject requestParameters = new JSONObject(paramsJson);
                            sessionRequestParams = new SessionRequestParams(requestParameters);
                        } else {
                            YLogger.d("SessionRequestParameters is empty sessionID=%d, SessionType=%s",
                                id, sessionArguments.getType());
                        }
                    } catch (Throwable e) {
                        YLogger.e(e, "Something was wrong while getting session's request parameters.");
                    }
                }
            }
        }
        return sessionRequestParams;
    }

    @Override
    @NonNull
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", creationTime=" + creationTime +
                ", currentReportId=" + currentReportId +
                ", sessionRequestParams=" + sessionRequestParams +
                ", sleepStart=" + sleepStart +
                '}';
    }

    @VisibleForTesting
    public long getSleepStart() {
        return sleepStart;
    }
}
