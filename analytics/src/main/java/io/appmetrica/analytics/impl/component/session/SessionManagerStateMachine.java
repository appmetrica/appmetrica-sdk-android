package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public class SessionManagerStateMachine {

    private static final String TAG = "[SessionManagerFSM]";

    public interface EventSaver {
        void saveEvent(@NonNull final CounterReport reportData, @NonNull final SessionState sessionState);
    }

    public enum State {
        EMPTY, BACKGROUND, FOREGROUND
    }

    @NonNull private final ComponentUnit mComponent;
    @NonNull private final SessionIDProvider sessionIDProvider;
    @NonNull private final EventSaver mSaver;
    @NonNull private final ExtraMetaInfoRetriever mExtraMetaInfoRetriever;

    @NonNull private final ISessionFactory<SessionArguments> mForegroundSessionFactory;
    @NonNull private final ISessionFactory<SessionArguments> mBackgroundSessionFactory;

    @Nullable private Session mCurrentSession;
    @Nullable private State mState = null;

    public SessionManagerStateMachine(@NonNull ComponentUnit component,
                                      @NonNull SessionIDProvider sessionIDProvider,
                                      @NonNull EventSaver saver) {
        this(
                component,
                sessionIDProvider,
                saver,
                new ForegroundSessionFactory(component, sessionIDProvider),
                new BackgroundSessionFactory(component, sessionIDProvider),
                new ExtraMetaInfoRetriever(component.getContext())
        );
    }

    @VisibleForTesting
    //todo revert package private access after removing strange pseudo complex pseudo unit tests
    public SessionManagerStateMachine(@NonNull ComponentUnit component,
                                      @NonNull SessionIDProvider sessionIDProvider,
                                      @NonNull EventSaver saver,
                                      @NonNull ISessionFactory<SessionArguments> foregroundSessionFactory,
                                      @NonNull ISessionFactory<SessionArguments> backgroundSessionFactory,
                                      @NonNull ExtraMetaInfoRetriever extraMetaInfoRetriever) {
        mComponent = component;
        mSaver = saver;

        mForegroundSessionFactory = foregroundSessionFactory;
        mBackgroundSessionFactory = backgroundSessionFactory;
        this.sessionIDProvider = sessionIDProvider;
        mExtraMetaInfoRetriever = extraMetaInfoRetriever;
    }

    public synchronized void heartbeat(@NonNull CounterReport reportData) {
        DebugLogger.info(TAG, mComponent.getComponentId() + " heartbeat");
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
        getSomeSession(report).updateAliveReportNeeded(false);
        if (mState != State.EMPTY) {
            close(mCurrentSession, report);
        }
        mState = State.EMPTY;
    }

    @NonNull
    public synchronized Session getSomeSession(@NonNull CounterReport report) {
        loadValidSession(report);
        if (mState != State.EMPTY && checkValidityOrClose(mCurrentSession, report) == false) {
            DebugLogger.info(TAG, mComponent.getComponentId() + " session %s is invalid", mCurrentSession);
            mState = State.EMPTY;
            mCurrentSession = null;
        }
        DebugLogger.info(TAG, mComponent.getComponentId() + " getSomeSession. current state is %s", mState);
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
    public SessionState createBackgroundSessionStub(final long reportTimestampSeconds) {
        long sessionId = sessionIDProvider.getNextSessionId();
        long currentReportId = SessionDefaults.INITIAL_REPORT_ID;
        mComponent.getDbHelper().newSession(sessionId, SessionType.BACKGROUND, reportTimestampSeconds);
        return new SessionState()
                .withSessionId(sessionId)
                .withSessionType(SessionType.BACKGROUND)
                .withReportId(currentReportId)
                .withReportTime(0);
    }

    @NonNull
    private Session createForegroundSession(@NonNull CounterReport reportData) {
        DebugLogger.info(TAG, mComponent.getComponentId() + " create foreground session");
        final PublicLogger logger = mComponent.getPublicLogger();
        if (logger.isEnabled()) {
            logger.i("Start foreground session");
        }
        long eventCreationElapsedRealtime = reportData.getCreationElapsedRealtime();
        Session session = mForegroundSessionFactory.create(
                new SessionArguments(eventCreationElapsedRealtime,
                    reportData.getCreationTimestamp())
        );
        mState = State.FOREGROUND;

        mComponent.getEventTrigger().trigger();
        mSaver.saveEvent(CounterReport.formSessionStartReportData(reportData, mExtraMetaInfoRetriever),
                getStateFromSession(session, eventCreationElapsedRealtime));
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
        if (mState == null) {
            Session foregroundSession = mForegroundSessionFactory.load();
            if (checkValidity(foregroundSession, report) == false) {
                return foregroundSession;
            } else {
                Session backgroundSession = mBackgroundSessionFactory.load();
                if (checkValidity(backgroundSession, report) == false) {
                    return backgroundSession;
                } else {
                    return null;
                }
            }
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

    private boolean checkValidity(@Nullable Session session, @NonNull CounterReport reportData) {
        if (session == null) {
            return false;
        } else {
            return session.isValid(reportData.getCreationElapsedRealtime());
        }
    }

    private void close(@NonNull Session session, @Nullable CounterReport reportData) {
        if (session.isAliveNeeded()) {
            mSaver.saveEvent(CounterReport.formAliveReportData(reportData), getAliveReportSessionState(session));
            session.updateAliveReportNeeded(false);
        }
        DebugLogger.info(TAG, mComponent.getComponentId() + " stop session %d type %s", session.getId(),
                session.getType().toString());
        final PublicLogger logger = mComponent.getPublicLogger();
        if (logger.isEnabled()) {
            switch (session.getType()) {
                case BACKGROUND:
                    logger.i("Finish background session");
                    break;
                case FOREGROUND:
                    logger.i("Finish foreground session");
                    break;
            }
        }
        session.stopSession();
    }

    @NonNull private Session createBackgroundSession(@NonNull CounterReport reportData) {
        DebugLogger.info(TAG, mComponent.getComponentId() + " create background session");
        final PublicLogger logger = mComponent.getPublicLogger();
        if (logger.isEnabled()) {
            logger.i("Start background session");
        }
        mState = State.BACKGROUND;
        long eventCreationElapsedRealtime = reportData.getCreationElapsedRealtime();
        Session session = mBackgroundSessionFactory.create(
                new SessionArguments(eventCreationElapsedRealtime,
                        reportData.getCreationTimestamp())
        );
        //non-elegant solution for first event
        if (mComponent.getVitalComponentDataProvider().isFirstEventDone()) {
            mSaver.saveEvent(
                    CounterReport.formSessionStartReportData(reportData, mExtraMetaInfoRetriever),
                    getStateFromSession(session, reportData.getCreationElapsedRealtime())
            );
        } else if (reportData.getType() == InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId()) {
            mSaver.saveEvent(reportData, getStateFromSession(session, eventCreationElapsedRealtime));
            mSaver.saveEvent(
                    CounterReport.formSessionStartReportData(reportData, mExtraMetaInfoRetriever),
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

    @NonNull private SessionState getStateFromSession(@NonNull Session currentSession, long creationElapsedRealtime) {
        return new SessionState()
                .withSessionId(currentSession.getId())
                .withReportId(currentSession.getNextReportId())
                .withReportTime(currentSession.getAndUpdateLastEventTimeSeconds(creationElapsedRealtime))
                .withSessionType(currentSession.getType());
    }

    @NonNull
    public SessionState peekCurrentSessionState(@NonNull CounterReport report) {
        Session lastSession = loadLastSession(report);
        if (lastSession != null) {
            return new SessionState()
                    .withSessionId(lastSession.getId())
                    .withReportId(lastSession.getNextReportId())
                    .withReportTime(lastSession.getLastEventTimeOffsetSeconds())
                    .withSessionType(lastSession.getType());
        } else {
            DebugLogger.warning(TAG, "Could not load session, creating background stub.");
            return createBackgroundSessionStub(report.getCreationTimestamp());
        }
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
