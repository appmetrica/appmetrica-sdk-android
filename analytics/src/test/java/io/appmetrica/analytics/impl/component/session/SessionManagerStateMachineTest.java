package io.appmetrica.analytics.impl.component.session;

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.events.ConditionalEventTrigger;
import io.appmetrica.analytics.impl.utils.ServerTime;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import org.mockito.Mockito;
import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.androidtestutils.rules.ContextRule;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule;
import io.appmetrica.gradle.testutils.rules.MockedStaticRule;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionManagerStateMachineTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    private SessionManagerStateMachine mManager;

    @Mock
    private PublicLogger publicLogger;
    @Mock
    private ComponentUnit mComponentUnit;
    @Mock
    private ISessionFactory<SessionArguments> mFgSessionFactory;
    @Mock
    private ISessionFactory<SessionArguments> mBgSessionFactory;
    @Mock
    private ISessionFactory<SessionArguments> mSessionFromPastFactory;
    @Mock
    private Session mFgSession;
    @Mock
    private Session mBgSession;
    @Mock
    private CounterReport mCounterReport;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    private DatabaseHelper mDatabaseHelper;

    @Rule
    public final MockedConstructionRule<CounterReport> counterReportMockRule =
        new MockedConstructionRule<>(CounterReport.class);

    @Rule
    public final MockedStaticRule<CounterReport> counterReportStaticRule = new MockedStaticRule<>(CounterReport.class);

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mComponentUnit.getContext()).thenReturn(globalServiceLocatorRule.getContext());
        when(mComponentUnit.getVitalComponentDataProvider()).thenReturn(vitalComponentDataProvider);
        when(mComponentUnit.getEventTrigger()).thenReturn(mock(ConditionalEventTrigger.class));
        when(mComponentUnit.getDbHelper()).thenReturn(mDatabaseHelper);
        when(mComponentUnit.getPublicLogger()).thenReturn(publicLogger);
        when(mFgSessionFactory.load()).thenReturn(mFgSession);
        when(mBgSessionFactory.load()).thenReturn(mBgSession);
        when(mFgSession.getType()).thenReturn(SessionType.FOREGROUND);
        when(mBgSession.getType()).thenReturn(SessionType.BACKGROUND);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mock(SessionManagerStateMachine.EventSaver.class),
            new MockSessionFactory(SessionType.FOREGROUND),
            new MockSessionFactory(SessionType.BACKGROUND),
            mSessionFromPastFactory
        );
        ServerTime.getInstance().init();
    }

    @Test
    public void testCreateBackgroundSession() {
        mManager.getSomeSession(mCounterReport);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.BACKGROUND);
        verify(mComponentUnit.getPublicLogger()).info("Start background session");
    }

    @Test
    public void testCreateForeground() {
        mManager.heartbeat(mCounterReport);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.FOREGROUND);
        verify(mComponentUnit.getPublicLogger()).info("Start foreground session");
    }

    @Test
    public void testCreateBackgroundSessionIfLoggerDisabled() {
        mManager.getSomeSession(mCounterReport);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.BACKGROUND);
    }

    @Test
    public void testCreateForegroundIfLoggerDisabled() {
        mManager.heartbeat(mCounterReport);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.FOREGROUND);
    }

    @Test
    public void testCreateNewForeground() throws InterruptedException {
        mManager.heartbeat(mCounterReport);
        Session oldSession = mManager.getSomeSession(mCounterReport);
        long oldSessionId = oldSession.getId();

        doReturn(false).when(oldSession).isValid(anyLong());

        mManager.heartbeat(mCounterReport);
        assertThat(mManager.getSomeSession(mCounterReport).getId()).isNotEqualTo(oldSessionId);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.FOREGROUND);
    }

    @Test
    public void testToggleForegroundToBackground() throws InterruptedException {
        mManager.heartbeat(mCounterReport);
        Session oldSession = mManager.getSomeSession(mCounterReport);
        long oldSessionType = oldSession.getType().getCode();
        doReturn(false).when(oldSession).isValid(anyLong());

        assertThat(mManager.getSomeSession(mCounterReport).getType().getCode()).isNotEqualTo(oldSessionType);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.BACKGROUND);
    }

    @Test
    public void testToggleBackgroundToForeground() {
        CounterReport aliveReport = mock(CounterReport.class);
        when(CounterReport.formAliveReportData(mCounterReport)).thenReturn(aliveReport);
        mManager.getSomeSession(mCounterReport);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.BACKGROUND);
        assertThat(mManager.getSomeSession(mCounterReport).getType()).isEqualTo(SessionType.BACKGROUND);

        final SessionState current = mManager.getCurrentSessionState(mCounterReport);

        mManager.heartbeat(mCounterReport);

        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.FOREGROUND);
        assertThat(mManager.getSomeSession(mCounterReport).getType()).isEqualTo(SessionType.FOREGROUND);

        verify(mManager.getSaver(), times(1)).saveEvent(
            eq(aliveReport),
            argThat(argument ->
                argument.getSessionId() == current.getSessionId()
                    && argument.getSessionType() == current.getSessionType()
            )
        );
    }

    @Test
    public void testCrashInBackground() {
        Session session = mManager.getSomeSession(mCounterReport);
        mManager.stopCurrentSessionDueToCrash(mCounterReport);
        assertThat(mManager.getState()).isNull();
        verify(session).markSessionAsCrashed();
        verify(session).updateAliveReportNeeded(false);
        verify(mComponentUnit.getPublicLogger()).info("Start background session");
    }

    @Test
    public void testCrashInForeground() {
        mManager.heartbeat(mCounterReport);
        Session session = mManager.getSomeSession(mCounterReport);
        mManager.stopCurrentSessionDueToCrash(mCounterReport);
        assertThat(mManager.getState()).isNull();
        verify(session).markSessionAsCrashed();
        verify(session).updateAliveReportNeeded(false);
        verify(mComponentUnit.getPublicLogger()).info("Start foreground session");
    }

    @Test
    public void testCrashInBackgroundIfLoggerIsDisabled() {
        Session session = mManager.getSomeSession(mCounterReport);
        mManager.stopCurrentSessionDueToCrash(mCounterReport);
        verify(session).markSessionAsCrashed();
        verify(session).updateAliveReportNeeded(false);
    }

    @Test
    public void testCrashInForegroundIfLoggerIsDisabled() {
        mManager.heartbeat(mCounterReport);
        Session session = mManager.getSomeSession(mCounterReport);
        mManager.stopCurrentSessionDueToCrash(mCounterReport);
        verify(session).markSessionAsCrashed();
        verify(session).updateAliveReportNeeded(false);
    }

    @Test
    public void testGetBackgroundAndUpdateLastActiveTime() {
        mManager.getSomeSession(mCounterReport);
        Session session = mManager.getSomeSession(mCounterReport);
        assertThat(session.getType()).isEqualTo(SessionType.BACKGROUND);
        verify(session, times(1)).updateLastActiveTime(anyLong());
    }

    @Test
    public void testNoUpdateLastActiveTimeForForegroundSession() {
        mManager.heartbeat(mCounterReport);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.FOREGROUND);
        Session session = mManager.getSomeSession(mCounterReport);
        assertThat(session.getType()).isEqualTo(SessionType.FOREGROUND);
        verify(session, never()).updateLastActiveTime(anyLong());
    }

    @Test
    public void testHeartbeatForForegroundSession() {
        mManager.heartbeat(mCounterReport);
        Session session = mManager.getSomeSession(mCounterReport);
        mManager.heartbeat(mCounterReport);
        assertThat(session.getType()).isEqualTo(SessionType.FOREGROUND);
        verify(session, times(1)).updateLastActiveTime(anyLong());
    }

    @Test
    public void testGetCurrentSessionStateTimeOffsetIfReportDataDoesNotContainCreationElapsedRealtime() {
        SystemTimeProvider systemTimeProvider = mock(SystemTimeProvider.class);
        when(systemTimeProvider.elapsedRealtime()).thenReturn(0L);
        CounterReport counterReport = new CounterReport("Test value", "Test event",
            InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
        assertThat(mManager.getCurrentSessionState(counterReport).getReportTime()).isLessThan(1000);
    }

    @Test
    public void testGetCurrentSessionStateTimeOffsetIfReportContainsElapsedReatime() {
        long creationElapsedRealtime = TimeUnit.SECONDS.toMillis(61);
        CounterReport counterReport = mock();
        when(counterReport.getCreationElapsedRealtime()).thenReturn(creationElapsedRealtime);
        SessionState sessionState = mManager.getCurrentSessionState(counterReport);
        long sessionStateTime = sessionState.getReportTime();
        assertThat(sessionStateTime).isEqualTo(61);
    }

    @Test
    public void testPeekCurrentSessionStateNull() {
        long sessionId = 42424242;
        when(mFgSession.isValid(anyLong())).thenReturn(true);
        when(mBgSession.isValid(anyLong())).thenReturn(true);
        when(mFgSession.getId()).thenReturn(sessionId);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory,
            mSessionFromPastFactory
        );
        SessionState currentSessionState = mManager.peekCurrentSessionState(mCounterReport);
        assertThat(currentSessionState.getSessionId()).isEqualTo(sessionId);
        assertThat(currentSessionState.getSessionType()).isEqualTo(SessionType.FOREGROUND);
        assertThat(currentSessionState.getReportTime()).isEqualTo(0);
        assertThat(currentSessionState.getReportId()).isEqualTo(SessionDefaults.INITIAL_REPORT_ID);
    }

    @Test
    public void testPeekCurrentSessionStateForeground() {
        long sessionId = 3333333;
        long lastEventTimeOffset = 2222222;
        long reportId = 1111;
        when(mFgSession.getId()).thenReturn(sessionId);
        when(mFgSession.isValid(anyLong())).thenReturn(false);
        when(mFgSession.getEventTimeOffsetForPrevSession(anyLong(), anyLong())).thenReturn(lastEventTimeOffset);
        when(mFgSession.getNextReportId()).thenReturn(reportId);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory,
            mSessionFromPastFactory
        );
        SessionState currentSessionState = mManager.peekCurrentSessionState(mCounterReport);
        assertThat(currentSessionState.getSessionId()).isEqualTo(sessionId);
        assertThat(currentSessionState.getSessionType()).isEqualTo(SessionType.FOREGROUND);
        assertThat(currentSessionState.getReportTime()).isEqualTo(lastEventTimeOffset);
        assertThat(currentSessionState.getReportId()).isEqualTo(reportId);
    }

    @Test
    public void testPeekCurrentSessionStateBackground() {
        long sessionId = 3333333;
        long lastEventTimeOffset = 2222222;
        long reportId = 1111;
        when(mFgSession.isValid(anyLong())).thenReturn(true);
        when(mBgSession.isValid(anyLong())).thenReturn(false);
        when(mBgSession.getId()).thenReturn(sessionId);
        when(mBgSession.getEventTimeOffsetForPrevSession(anyLong(), anyLong())).thenReturn(lastEventTimeOffset);
        when(mBgSession.getNextReportId()).thenReturn(reportId);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory,
            mSessionFromPastFactory
        );
        SessionState currentSessionState = mManager.peekCurrentSessionState(mCounterReport);
        assertThat(currentSessionState.getSessionId()).isEqualTo(sessionId);
        assertThat(currentSessionState.getSessionType()).isEqualTo(SessionType.BACKGROUND);
        assertThat(currentSessionState.getReportTime()).isEqualTo(lastEventTimeOffset);
        assertThat(currentSessionState.getReportId()).isEqualTo(reportId);
    }

    @Test
    public void testPeekCurrentSessionStateNullWhenNoSession() {
        when(mFgSessionFactory.load()).thenReturn(null);
        when(mBgSessionFactory.load()).thenReturn(null);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory,
            mSessionFromPastFactory
        );
        assertThat(mManager.peekCurrentSessionState(mCounterReport)).isNull();
    }

    @Test
    public void testPeekCurrentSessionStateCrashedSession() {
        long sessionId = 5555555L;
        long fromPastSessionId = 7777777L;
        long reportTimestamp = 1700000000000L;
        long reportElapsedRealtime = 12345L;
        SessionRequestParams sessionRequestParams = Mockito.mock(SessionRequestParams.class);
        Session fromPastSession = mock(Session.class);
        when(fromPastSession.getId()).thenReturn(fromPastSessionId);
        when(fromPastSession.getType()).thenReturn(SessionType.BACKGROUND);
        when(fromPastSession.getNextReportId()).thenReturn(SessionDefaults.INITIAL_REPORT_ID);
        when(fromPastSession.getAndUpdateLastEventTimeSeconds(anyLong())).thenReturn(0L);

        when(mFgSession.getId()).thenReturn(sessionId);
        when(mFgSession.isSessionCrashed()).thenReturn(true);
        when(mDatabaseHelper.getSessionRequestParams(sessionId, SessionType.FOREGROUND))
            .thenReturn(sessionRequestParams);
        when(mCounterReport.getCreationTimestamp()).thenReturn(reportTimestamp);
        when(mCounterReport.getCreationElapsedRealtime()).thenReturn(reportElapsedRealtime);
        when(mSessionFromPastFactory.create(any())).thenReturn(fromPastSession);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory,
            mSessionFromPastFactory
        );

        SessionState result = mManager.peekCurrentSessionState(mCounterReport);

        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo(fromPastSessionId);
        assertThat(result.getSessionType()).isEqualTo(SessionType.BACKGROUND);
        assertThat(result.getReportId()).isEqualTo(SessionDefaults.INITIAL_REPORT_ID);
        assertThat(result.getReportTime()).isEqualTo(0);
        verify(mSessionFromPastFactory).create(argThat(args ->
            args.getCreationElapsedRealtime() == reportElapsedRealtime
                && args.getCreationTimestamp() == reportTimestamp
                && args.getSessionRequestParams() == sessionRequestParams
        ));
    }

    @Test
    public void testPeekCurrentSessionStateStateWasNotNull() {
        Session newSession = mock(Session.class);
        long sessionId = 3333333;
        long lastEventTimeOffset = 2222222;
        long reportId = 1111;
        when(newSession.getId()).thenReturn(sessionId);
        when(newSession.getEventTimeOffsetForPrevSession(anyLong(), anyLong())).thenReturn(lastEventTimeOffset);
        when(newSession.getNextReportId()).thenReturn(reportId);
        when(newSession.getType()).thenReturn(SessionType.FOREGROUND);
        when(mFgSessionFactory.create(any())).thenReturn(newSession);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory,
            mSessionFromPastFactory
        );
        mManager.heartbeat(new CounterReport());
        SessionState currentSessionState = mManager.peekCurrentSessionState(mCounterReport);
        assertThat(currentSessionState.getSessionId()).isEqualTo(sessionId);
        assertThat(currentSessionState.getSessionType()).isEqualTo(SessionType.FOREGROUND);
        assertThat(currentSessionState.getReportTime()).isEqualTo(lastEventTimeOffset);
        assertThat(currentSessionState.getReportId()).isEqualTo(reportId);
    }

    @Test
    public void getThresholdIdIsMinLimitBeforeAnySessionIsProcessed() {
        assertThat(mManager.getThresholdSessionIdForActualSessions())
            .isEqualTo(SessionIDProvider.SESSION_ID_MIN_LIMIT);
    }

    @Test
    public void getThresholdIdIsFirstLoadedFgSessionIdAndDoesNotChangeOnRecreation() {
        long fgId = 1000L;
        long newFgId = 2000L;
        when(mFgSession.getId()).thenReturn(fgId);
        when(mFgSession.isValid(anyLong())).thenReturn(true);
        Session newFgSession = mock(Session.class);
        when(newFgSession.getId()).thenReturn(newFgId);
        when(newFgSession.getType()).thenReturn(SessionType.FOREGROUND);
        when(mFgSessionFactory.create(any())).thenReturn(newFgSession);
        SessionManagerStateMachine manager = new SessionManagerStateMachine(
            mComponentUnit,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory,
            mSessionFromPastFactory
        );
        manager.heartbeat(mCounterReport);

        assertThat(manager.getThresholdSessionIdForActualSessions()).isEqualTo(fgId);

        // Session is recreated — threshold stays at the first loaded session's ID
        when(mFgSession.isValid(anyLong())).thenReturn(false);
        manager.heartbeat(mCounterReport);

        assertThat(manager.getThresholdSessionIdForActualSessions()).isEqualTo(fgId);
    }

    @Test
    public void getThresholdIdIsMinOfFgAndBgWhenFgInvalidAndHasHigherId() {
        // fg was created later (higher ID) but is now expired; bg (lower ID) is still valid
        long fgId = 2000L;
        long bgId = 1000L;
        when(mFgSession.getId()).thenReturn(fgId);
        when(mBgSession.getId()).thenReturn(bgId);
        when(mFgSession.isValid(anyLong())).thenReturn(false);
        when(mBgSession.isValid(anyLong())).thenReturn(true);
        SessionManagerStateMachine manager = new SessionManagerStateMachine(
            mComponentUnit,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory,
            mSessionFromPastFactory
        );
        manager.getSomeSession(mCounterReport);
        assertThat(manager.getThresholdSessionIdForActualSessions()).isEqualTo(bgId);
    }

    @Test
    public void getThresholdIdIsCreatedSessionIdWhenNoSessionsLoaded() {
        when(mFgSessionFactory.load()).thenReturn(null);
        when(mBgSessionFactory.load()).thenReturn(null);
        long createdBgId = 500L;
        Session createdBg = mock(Session.class);
        when(createdBg.getId()).thenReturn(createdBgId);
        when(createdBg.getType()).thenReturn(SessionType.BACKGROUND);
        when(mBgSessionFactory.create(any())).thenReturn(createdBg);
        SessionManagerStateMachine manager = new SessionManagerStateMachine(
            mComponentUnit,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory,
            mSessionFromPastFactory
        );
        manager.getSomeSession(mCounterReport);
        assertThat(manager.getThresholdSessionIdForActualSessions()).isEqualTo(createdBgId);
    }
}
