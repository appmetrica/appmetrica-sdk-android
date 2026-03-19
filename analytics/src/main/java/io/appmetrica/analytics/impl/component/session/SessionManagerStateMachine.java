package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class SessionManagerStateMachine {

    private static final String TAG = "[SessionManagerFSM]";

    public interface EventSaver {
        void saveEvent(@NonNull final CounterReport reportData, @NonNull final SessionState sessionState);
    }

    public enum State {
        EMPTY, BACKGROUND, FOREGROUND
    }

    @NonNull
    private final ComponentUnit mComponent;
    @NonNull
    private final EventSaver mSaver;

    @NonNull
    private final ISessionFactory<SessionArguments> mForegroundSessionFactory;
    @NonNull
    private final ISessionFactory<SessionArguments> mBackgroundSessionFactory;
    @NonNull
    private final ISessionFactory<SessionArguments> sessionFromPastFactory;

    @Nullable
    private Session mCurrentSession;
    @Nullable
    private State mState = null;

    public SessionManagerStateMachine(@NonNull ComponentUnit component,
                                      @NonNull SessionIDProvider sessionIDProvider,
                                      @NonNull EventSaver saver) {
        this(
            component,
            saver,
            new ForegroundSessionFactory(component, sessionIDProvider),
            new BackgroundSessionFactory(component, sessionIDProvider),
            new SessionFromPastFactory(component, sessionIDProvider)
        );
    }

    @VisibleForTesting
    //todo revert package private access after removing strange pseudo complex pseudo unit tests
    public SessionManagerStateMachine(@NonNull ComponentUnit component,
                                      @NonNull EventSaver saver,
                                      @NonNull ISessionFactory<SessionArguments> foregroundSessionFactory,
                                      @NonNull ISessionFactory<SessionArguments> backgroundSessionFactory,
                                      @NonNull ISessionFactory<SessionArguments> sessionFromPastFactory) {
        mComponent = component;
        mSaver = saver;
        mForegroundSessionFactory = foregroundSessionFactory;
        mBackgroundSessionFactory = backgroundSessionFactory;
        this.sessionFromPastFactory = sessionFromPastFactory;
    }

    public synchronized void heartbeat(@NonNull CounterReport reportData) {
        DebugLogger.INSTANCE.info(TAG, mComponent.getComponentId() + " heartbeat");
        loadValidSession(reportData);
        switch (mState) {
            case FOREGROUND:
                if (checkValidityOrClose(mCurrentSession, reportData)) {
                    mCurrentSession.updateLastActiveTime(reportData.getCreationElapsedRealtime());
                } else {
                    mCurrentSession = createForegroundSession(reportData);
                }
                break;
            case BACKGROUND:
                close(mCurrentSession, reportData);
                mCurrentSession = createForegroundSession(reportData);
                break;
            case EMPTY:
                mCurrentSession = createForegroundSession(reportData);
                break;
        }
    }

    public synchronized void stopCurrentSessionDueToCrash(@NonNull CounterReport report) {
        DebugLogger.INSTANCE.info(TAG, mComponent.getComponentId() + " stopCurrentSessionDueToCrash");
        Session lastSession = loadLastSession(report);
        if (lastSession != null) {
            DebugLogger.INSTANCE.info(
                TAG,
                "%s mark session with id %s as crashed",
                mComponent.getComponentId(),
                lastSession.getId()
            );
            lastSession.markSessionAsCrashed();
            lastSession.updateAliveReportNeeded(false);
            mState = null;
        } else {
            DebugLogger.INSTANCE.info(
                TAG,
                "%s no last session found to stop due crash",
                mComponent.getComponentId()
            );
        }
    }

    @NonNull
    public synchronized Session getSomeSession(@NonNull CounterReport report) {
        loadValidSession(report);
        DebugLogger.INSTANCE.info(
            TAG,
            mComponent.getComponentId() + " getSomeSession for report with type: %s and name = %s. current state is %s",
            report.getType(),
            report.getName(),
            mCurrentSession
        );
        if (mState != State.EMPTY && !checkValidityOrClose(mCurrentSession, report)) {
            DebugLogger.INSTANCE.info(
                TAG,
                mComponent.getComponentId() + " session %s is invalid",
                mCurrentSession
            );
            mState = State.EMPTY;
            mCurrentSession = null;
        }
        DebugLogger.INSTANCE.info(
            TAG,
            mComponent.getComponentId() + " getSomeSession. current state is %s",
            mState
        );
        switch (mState) {
            case FOREGROUND:
                return mCurrentSession;
            case BACKGROUND:
                mCurrentSession.updateLastActiveTime(report.getCreationElapsedRealtime());
                return mCurrentSession;
            case EMPTY:
            default: // default == State.EMPTY
                mCurrentSession = createBackgroundSession(report);
                return mCurrentSession;
        }
    }

    @NonNull
    public SessionState getCurrentSessionState(@NonNull CounterReport reportData) {
        Session currentSession = getSomeSession(reportData);
        return getStateFromSession(currentSession, reportData.getCreationElapsedRealtime());
    }

    public synchronized long getThresholdSessionIdForActualSessions() {
        return mCurrentSession == null ? SessionIDProvider.SESSION_ID_MIN_LIMIT : mCurrentSession.getId() - 1;
    }

    @NonNull
    public SessionState createBackgroundSessionFromPast(
        final long reportElapsedRealtime,
        final long reportTimestampSeconds,
        @NonNull SessionRequestParams sessionRequestParams
    ) {
        mState = State.BACKGROUND;
        mCurrentSession = sessionFromPastFactory.create(
            new SessionArguments(
                reportElapsedRealtime,
                reportTimestampSeconds,
                sessionRequestParams
            )
        );
        return getStateFromSession(mCurrentSession, reportElapsedRealtime);
    }

    @NonNull
    private Session createForegroundSession(@NonNull CounterReport reportData) {
        DebugLogger.INSTANCE.info(TAG, mComponent.getComponentId() + " create foreground session");
        final PublicLogger logger = mComponent.getPublicLogger();
        logger.info("Start foreground session");
        long eventCreationElapsedRealtime = reportData.getCreationElapsedRealtime();
        Session session = mForegroundSessionFactory.create(
            new SessionArguments(eventCreationElapsedRealtime,
                reportData.getCreationTimestamp())
        );
        mState = State.FOREGROUND;

        mComponent.getEventTrigger().trigger();
        mSaver.saveEvent(
            CounterReport.formSessionStartReportData(
                reportData,
                GlobalServiceLocator.getInstance().getExtraMetaInfoRetriever()
            ),
            getStateFromSession(session, eventCreationElapsedRealtime)
        );
        return session;
    }

    private void loadValidSession(@NonNull CounterReport reportData) {
        if (mState == null) {
            Session foregroundSession = mForegroundSessionFactory.load();
            if (checkValidityOrClose(foregroundSession, reportData)) {
                mCurrentSession = foregroundSession;
                mState = State.FOREGROUND;
            } else {
                Session backgroundSession = mBackgroundSessionFactory.load();
                if (checkValidityOrClose(backgroundSession, reportData)) {
                    mCurrentSession = backgroundSession;
                    mState = State.BACKGROUND;
                } else {
                    mCurrentSession = null;
                    mState = State.EMPTY;
                }
            }
        }
    }

    @Nullable
    private Session loadLastSession(@NonNull CounterReport report) {
        DebugLogger.INSTANCE.info(
            TAG,
            "loadLastSession: mState = %s; mCurrentSession = %s",
            mState,
            mCurrentSession
        );
        if (mState == null) {
            Session foregroundSession = mForegroundSessionFactory.load();
            Session backgroundSession = mBackgroundSessionFactory.load();
            DebugLogger.INSTANCE.info(
                TAG,
                "loadLastSession: foregroundSession = %s; backgroundSession = %s; report: %s",
                foregroundSession,
                backgroundSession,
                report
            );
            long foregroundSessionId = foregroundSession == null ? -1 : foregroundSession.getId();
            long backgroundSessionId = backgroundSession == null ? -1 : backgroundSession.getId();
            return foregroundSessionId > backgroundSessionId ? foregroundSession : backgroundSession;
        } else {
            return mCurrentSession;
        }
    }

    private boolean checkValidityOrClose(@Nullable Session session, @NonNull CounterReport reportData) {
        if (session == null) {
            return false;
        } else {
            if (session.isValid(reportData.getCreationElapsedRealtime())) {
                return true;
            } else {
                close(session, reportData);
                return false;
            }
        }
    }

    private void close(@NonNull Session session, @Nullable CounterReport reportData) {
        if (session.isAliveNeeded()) {
            mSaver.saveEvent(CounterReport.formAliveReportData(reportData), getAliveReportSessionState(session));
            session.updateAliveReportNeeded(false);
        }
        DebugLogger.INSTANCE.info(
            TAG,
            mComponent.getComponentId() + " stop session %d type %s",
            session.getId(),
            session.getType().toString()
        );
        final PublicLogger logger = mComponent.getPublicLogger();
        switch (session.getType()) {
            case BACKGROUND:
                logger.info("Finish background session");
                break;
            case FOREGROUND:
                logger.info("Finish foreground session");
                break;
        }
        session.stopSession();
    }

    @NonNull
    private Session createBackgroundSession(@NonNull CounterReport reportData) {
        DebugLogger.INSTANCE.info(TAG, mComponent.getComponentId() + " create background session");
        final PublicLogger logger = mComponent.getPublicLogger();
        logger.info("Start background session");
        mState = State.BACKGROUND;
        long eventCreationElapsedRealtime = reportData.getCreationElapsedRealtime();
        Session session = mBackgroundSessionFactory.create(
            new SessionArguments(eventCreationElapsedRealtime,
                reportData.getCreationTimestamp())
        );
        //non-elegant solution for first event
        if (mComponent.getVitalComponentDataProvider().isFirstEventDone()) {
            mSaver.saveEvent(
                CounterReport.formSessionStartReportData(
                    reportData,
                    GlobalServiceLocator.getInstance().getExtraMetaInfoRetriever()
                ),
                getStateFromSession(session, reportData.getCreationElapsedRealtime())
            );
        } else if (reportData.getType() == InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId()) {
            mSaver.saveEvent(reportData, getStateFromSession(session, eventCreationElapsedRealtime));
            mSaver.saveEvent(
                CounterReport.formSessionStartReportData(
                    reportData,
                    GlobalServiceLocator.getInstance().getExtraMetaInfoRetriever()
                ),
                getStateFromSession(session, eventCreationElapsedRealtime)
            );
        }
        return session;
    }

    @NonNull
    private SessionState getAliveReportSessionState(@NonNull final Session session) {
        return new SessionState()
            .withSessionId(session.getId())
            .withSessionType(session.getType())
            .withReportId(session.getNextReportId())
            .withReportTime(session.getAliveReportOffsetSeconds());
    }

    @NonNull
    private SessionState getStateFromSession(@NonNull Session currentSession, long creationElapsedRealtime) {
        return new SessionState()
            .withSessionId(currentSession.getId())
            .withReportId(currentSession.getNextReportId())
            .withReportTime(currentSession.getAndUpdateLastEventTimeSeconds(creationElapsedRealtime))
            .withSessionType(currentSession.getType());
    }

    @Nullable
    public SessionState peekCurrentSessionState(@NonNull CounterReport report) {
        Session lastSession = loadLastSession(report);
        DebugLogger.INSTANCE.info(
            TAG,
            "peekCurrentSessionState: lastSession = %s; counterReport = %s; creationTimestamp = %s",
            lastSession,
            report,
            report.getCreationTimestamp()
        );
        if (lastSession != null) {
            if (lastSession.isSessionCrashed()) {
                DebugLogger.INSTANCE.info(TAG, "peekCurrentSessionState: lastSession is crashed");
                SessionRequestParams requestParams =
                    mComponent.getDbHelper().getSessionRequestParams(lastSession.getId(), lastSession.getType());
                return createBackgroundSessionFromPast(
                    report.getCreationElapsedRealtime(),
                    report.getCreationTimestamp(),
                    requestParams
                );
            } else {
                return new SessionState()
                    .withSessionId(lastSession.getId())
                    .withReportId(lastSession.getNextReportId())
                    .withReportTime(lastSession.getEventTimeOffsetForPrevSession(
                        report.getCreationTimestamp(),
                        report.getCreationElapsedRealtime())
                    )
                    .withSessionType(lastSession.getType());
            }
        }
        return null;
    }

    @VisibleForTesting
    @Nullable
    State getState() {
        return mState;
    }

    @VisibleForTesting
    @NonNull
    public EventSaver getSaver() {
        return mSaver;
    }
}
