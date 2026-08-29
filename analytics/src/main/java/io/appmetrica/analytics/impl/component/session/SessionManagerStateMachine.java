package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class SessionManagerStateMachine {

    private static final String TAG = "[SessionManagerFSM]";

    // Protects sessions from synthetic rapid-switching scenarios (deeplinks, attributionId changes
    // can generate events for 3+ sessions simultaneously).
    @VisibleForTesting
    static final int SESSION_PROTECTION_WINDOW = 10;

    public interface EventSaver {
        void saveEvent(@NonNull final CoreServiceEvent serviceEvent, @NonNull final SessionState sessionState);
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
    @Nullable
    private Long firstSessionIdOfThisLaunch = null;

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

    public synchronized void heartbeat(@NonNull CoreServiceEvent serviceEvent) {
        DebugLogger.INSTANCE.info(TAG, mComponent.getComponentId() + " heartbeat");
        loadValidSession(serviceEvent);
        switch (mState) {
            case FOREGROUND:
                if (checkValidityOrClose(mCurrentSession, serviceEvent)) {
                    mCurrentSession.updateLastActiveTime(serviceEvent.getCreationElapsedRealtime());
                } else {
                    mCurrentSession = createForegroundSession(serviceEvent);
                }
                break;
            case BACKGROUND:
                close(mCurrentSession, serviceEvent);
                mCurrentSession = createForegroundSession(serviceEvent);
                break;
            case EMPTY:
                mCurrentSession = createForegroundSession(serviceEvent);
                break;
        }
    }

    public synchronized void stopCurrentSessionDueToCrash(@NonNull CoreServiceEvent serviceEvent) {
        DebugLogger.INSTANCE.info(TAG, mComponent.getComponentId() + " stopCurrentSessionDueToCrash");
        Session lastSession = loadLastSession(serviceEvent);
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
    public synchronized Session getSomeSession(@NonNull CoreServiceEvent serviceEvent) {
        loadValidSession(serviceEvent);
        DebugLogger.INSTANCE.info(
            TAG,
            mComponent.getComponentId()
                + " getSomeSession for serviceEvent with type: %s and name = %s. current state is %s",
            serviceEvent.getType(),
            serviceEvent.getName(),
            mCurrentSession
        );
        if (mState != State.EMPTY && !checkValidityOrClose(mCurrentSession, serviceEvent)) {
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
                mCurrentSession.updateLastActiveTime(serviceEvent.getCreationElapsedRealtime());
                return mCurrentSession;
            case EMPTY:
            default: // default == State.EMPTY
                mCurrentSession = createBackgroundSession(serviceEvent);
                return mCurrentSession;
        }
    }

    @NonNull
    public SessionState getCurrentSessionState(@NonNull CoreServiceEvent serviceEvent) {
        Session currentSession = getSomeSession(serviceEvent);
        return getStateFromSession(currentSession, serviceEvent.getCreationElapsedRealtime());
    }

    public synchronized long getThresholdSessionIdForActualSessions() {
        if (firstSessionIdOfThisLaunch == null) {
            return SessionIDProvider.SESSION_ID_MIN_LIMIT;
        }
        long currentSessionId = mCurrentSession != null
            ? mCurrentSession.getId()
            : firstSessionIdOfThisLaunch;
        return Math.max(firstSessionIdOfThisLaunch, currentSessionId - SESSION_PROTECTION_WINDOW);
    }

    private void updateFirstSessionIdOfThisLaunch(long sessionId) {
        if (firstSessionIdOfThisLaunch == null || sessionId < firstSessionIdOfThisLaunch) {
            firstSessionIdOfThisLaunch = sessionId;
        }
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
        updateFirstSessionIdOfThisLaunch(mCurrentSession.getId());
        return getStateFromSession(mCurrentSession, reportElapsedRealtime);
    }

    @NonNull
    private Session createForegroundSession(@NonNull CoreServiceEvent serviceEvent) {
        DebugLogger.INSTANCE.info(TAG, mComponent.getComponentId() + " create foreground session");
        final PublicLogger logger = mComponent.getPublicLogger();
        logger.info("Start foreground session");
        long eventCreationElapsedRealtime = serviceEvent.getCreationElapsedRealtime();
        Session session = mForegroundSessionFactory.create(
            new SessionArguments(eventCreationElapsedRealtime,
                serviceEvent.getCreationTimestamp())
        );
        updateFirstSessionIdOfThisLaunch(session.getId());
        mState = State.FOREGROUND;

        mComponent.getEventTrigger().trigger();
        mSaver.saveEvent(
            CoreServiceEvent.formSessionStartReportData(
                serviceEvent,
                GlobalServiceLocator.getInstance().getExtraMetaInfoRetriever().getBuildId()
            ),
            getStateFromSession(session, eventCreationElapsedRealtime)
        );
        return session;
    }

    private void loadValidSession(@NonNull CoreServiceEvent serviceEvent) {
        if (mState == null) {
            Session foregroundSession = mForegroundSessionFactory.load();
            if (foregroundSession != null) {
                updateFirstSessionIdOfThisLaunch(foregroundSession.getId());
            }
            if (checkValidityOrClose(foregroundSession, serviceEvent)) {
                mCurrentSession = foregroundSession;
                mState = State.FOREGROUND;
            } else {
                Session backgroundSession = mBackgroundSessionFactory.load();
                if (backgroundSession != null) {
                    updateFirstSessionIdOfThisLaunch(backgroundSession.getId());
                }
                if (checkValidityOrClose(backgroundSession, serviceEvent)) {
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
    private Session loadLastSession(@NonNull CoreServiceEvent serviceEvent) {
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
                "loadLastSession: foregroundSession = %s; backgroundSession = %s; serviceEvent: %s",
                foregroundSession,
                backgroundSession,
                serviceEvent
            );
            long foregroundSessionId = foregroundSession == null ? -1 : foregroundSession.getId();
            long backgroundSessionId = backgroundSession == null ? -1 : backgroundSession.getId();
            return foregroundSessionId > backgroundSessionId ? foregroundSession : backgroundSession;
        } else {
            return mCurrentSession;
        }
    }

    private boolean checkValidityOrClose(@Nullable Session session, @NonNull CoreServiceEvent serviceEvent) {
        if (session == null) {
            return false;
        } else {
            if (session.isValid(serviceEvent.getCreationElapsedRealtime())) {
                return true;
            } else {
                close(session, serviceEvent);
                return false;
            }
        }
    }

    private void close(@NonNull Session session, @Nullable CoreServiceEvent serviceEvent) {
        if (session.isAliveNeeded()) {
            mSaver.saveEvent(CoreServiceEvent.formAliveReportData(serviceEvent), getAliveReportSessionState(session));
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
    private Session createBackgroundSession(@NonNull CoreServiceEvent serviceEvent) {
        DebugLogger.INSTANCE.info(TAG, mComponent.getComponentId() + " create background session");
        final PublicLogger logger = mComponent.getPublicLogger();
        logger.info("Start background session");
        mState = State.BACKGROUND;
        long eventCreationElapsedRealtime = serviceEvent.getCreationElapsedRealtime();
        Session session = mBackgroundSessionFactory.create(
            new SessionArguments(eventCreationElapsedRealtime,
                serviceEvent.getCreationTimestamp())
        );
        updateFirstSessionIdOfThisLaunch(session.getId());
        //non-elegant solution for first event
        if (mComponent.getVitalComponentDataProvider().isFirstEventDone()) {
            mSaver.saveEvent(
                CoreServiceEvent.formSessionStartReportData(
                    serviceEvent,
                    GlobalServiceLocator.getInstance().getExtraMetaInfoRetriever().getBuildId()
                ),
                getStateFromSession(session, serviceEvent.getCreationElapsedRealtime())
            );
        } else if (serviceEvent.getType() == InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId()) {
            mSaver.saveEvent(serviceEvent, getStateFromSession(session, eventCreationElapsedRealtime));
            mSaver.saveEvent(
                CoreServiceEvent.formSessionStartReportData(
                    serviceEvent,
                    GlobalServiceLocator.getInstance().getExtraMetaInfoRetriever().getBuildId()
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
    public SessionState peekCurrentSessionState(@NonNull CoreServiceEvent serviceEvent) {
        Session lastSession = loadLastSession(serviceEvent);
        DebugLogger.INSTANCE.info(
            TAG,
            "peekCurrentSessionState: lastSession = %s; counterReport = %s; creationTimestamp = %s",
            lastSession,
            serviceEvent,
            serviceEvent.getCreationTimestamp()
        );
        if (lastSession != null) {
            if (lastSession.isSessionCrashed()) {
                DebugLogger.INSTANCE.info(TAG, "peekCurrentSessionState: lastSession is crashed");
                SessionRequestParams requestParams =
                    mComponent.getDbHelper().getSessionRequestParams(lastSession.getId(), lastSession.getType());
                return createBackgroundSessionFromPast(
                    serviceEvent.getCreationElapsedRealtime(),
                    serviceEvent.getCreationTimestamp(),
                    requestParams
                );
            } else {
                return new SessionState()
                    .withSessionId(lastSession.getId())
                    .withReportId(lastSession.getNextReportId())
                    .withReportTime(lastSession.getEventTimeOffsetForPrevSession(
                        serviceEvent.getCreationTimestamp(),
                        serviceEvent.getCreationElapsedRealtime())
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
