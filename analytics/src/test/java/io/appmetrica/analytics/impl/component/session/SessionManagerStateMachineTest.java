package io.appmetrica.analytics.impl.component.session;

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.CounterReportMatcher;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.events.ConditionalEventTrigger;
import io.appmetrica.analytics.impl.utils.ServerTime;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SessionManagerStateMachineTest extends CommonTest {

    private SessionManagerStateMachine mManager;

    @Mock
    private PublicLogger publicLogger;
    @Mock
    private ComponentUnit mComponentUnit;
    @Mock
    private ISessionFactory mFgSessionFactory;
    @Mock
    private ISessionFactory mBgSessionFactory;
    @Mock
    private Session mFgSession;
    @Mock
    private Session mBgSession;
    @Mock
    private SessionIDProvider mSessionIDProvider;
    @Mock
    private CounterReport mCounterReport;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mComponentUnit.getContext()).thenReturn(RuntimeEnvironment.getApplication());
        when(mComponentUnit.getVitalComponentDataProvider()).thenReturn(vitalComponentDataProvider);
        when(mComponentUnit.getEventTrigger()).thenReturn(mock(ConditionalEventTrigger.class));
        when(mComponentUnit.getDbHelper()).thenReturn(mock(DatabaseHelper.class));
        when(mComponentUnit.getPublicLogger()).thenReturn(publicLogger);
        when(mFgSessionFactory.load()).thenReturn(mFgSession);
        when(mBgSessionFactory.load()).thenReturn(mBgSession);
        when(mFgSession.getType()).thenReturn(SessionType.FOREGROUND);
        when(mBgSession.getType()).thenReturn(SessionType.BACKGROUND);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mSessionIDProvider,
            mock(SessionManagerStateMachine.EventSaver.class),
            new MockSessionFactory(SessionType.FOREGROUND),
            new MockSessionFactory(SessionType.BACKGROUND)
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
        mManager.getSomeSession(mCounterReport);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.BACKGROUND);
        assertThat(mManager.getSomeSession(mCounterReport).getType()).isEqualTo(SessionType.BACKGROUND);

        final SessionState current = mManager.getCurrentSessionState(mCounterReport);

        mManager.heartbeat(mCounterReport);

        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.FOREGROUND);
        assertThat(mManager.getSomeSession(mCounterReport).getType()).isEqualTo(SessionType.FOREGROUND);
        verify(mManager.getSaver(), times(1)).saveEvent(argThat(CounterReportMatcher.newMatcher().withType(InternalEvents.EVENT_TYPE_ALIVE)),
            argThat(new ArgumentMatcher<SessionState>() {
                @Override
                public boolean matches(SessionState argument) {
                    SessionState matchedState = argument;
                    return matchedState.getSessionId() == current.getSessionId() && matchedState.getSessionType() == current.getSessionType();
                }
            }));
    }

    @Test
    public void testCrashInBackground() {
        Session session = mManager.getSomeSession(mCounterReport);
        mManager.stopCurrentSessionDueToCrash(mCounterReport);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.EMPTY);
        verify(session).updateAliveReportNeeded(false);
        verify(mComponentUnit.getPublicLogger()).info("Start background session");
    }

    @Test
    public void testCrashInForeground() {
        mManager.heartbeat(mCounterReport);
        Session session = mManager.getSomeSession(mCounterReport);
        mManager.stopCurrentSessionDueToCrash(mCounterReport);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.EMPTY);
        verify(session).updateAliveReportNeeded(false);
        verify(mComponentUnit.getPublicLogger()).info("Start foreground session");
    }

    @Test
    public void testCrashInBackgroundIfLoggerIsDisabled() {
        Session session = mManager.getSomeSession(mCounterReport);
        mManager.stopCurrentSessionDueToCrash(mCounterReport);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.EMPTY);
        verify(session).updateAliveReportNeeded(false);
    }

    @Test
    public void testCrashInForegroundIfLoggerIsDisabled() {
        mManager.heartbeat(mCounterReport);
        Session session = mManager.getSomeSession(mCounterReport);
        mManager.stopCurrentSessionDueToCrash(mCounterReport);
        assertThat(mManager.getState()).isEqualTo(SessionManagerStateMachine.State.EMPTY);
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
            InternalEvents.EVENT_TYPE_REGULAR.getTypeId(), systemTimeProvider);
        assertThat(mManager.getCurrentSessionState(counterReport).getReportTime()).isLessThan(1000);
    }

    @Test
    public void testGetCurrentSessionStateTimeOffsetIfReportContainsElapsedReatime() {
        long creationElapsedRealtime = TimeUnit.SECONDS.toMillis(61);
        SystemTimeProvider systemTimeProvider = mock(SystemTimeProvider.class);
        when(systemTimeProvider.elapsedRealtime()).thenReturn(creationElapsedRealtime);
        CounterReport counterReport = new CounterReport("Test value", "Test event",
            InternalEvents.EVENT_TYPE_REGULAR.getTypeId(), systemTimeProvider);
        SessionState sessionState = mManager.getCurrentSessionState(counterReport);
        long sessionStateTime = sessionState.getReportTime();
        assertThat(sessionStateTime).isEqualTo(61);
    }

    @Test
    public void testGetCurrentSessionStateSessionIdIfReportDataContainsCreationTimestamp() {
        long currentTime = 34534543L;
        SystemTimeProvider systemTimeProvider = mock(SystemTimeProvider.class);
        when(systemTimeProvider.currentTimeMillis()).thenReturn(currentTime);
        CounterReport counterReport = new CounterReport("Test value", "Test event",
            InternalEvents.EVENT_TYPE_REGULAR.getTypeId(), systemTimeProvider);
        SessionState sessionState = mManager.getCurrentSessionState(counterReport);
        long sessionStateId = sessionState.getSessionId();
        assertThat(sessionStateId).isEqualTo(TimeUnit.MILLISECONDS.toSeconds(currentTime));
    }

    @Test
    public void testPeekCurrentSessionStateNull() {
        long nextSessionId = 42424242;
        when(mFgSession.isValid(anyLong())).thenReturn(true);
        when(mBgSession.isValid(anyLong())).thenReturn(true);
        when(mSessionIDProvider.getNextSessionId()).thenReturn(nextSessionId);
        when(mFgSession.getId()).thenReturn(nextSessionId);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mSessionIDProvider,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory
        );
        SessionState currentSessionState = mManager.peekCurrentSessionState(mCounterReport);
        assertThat(currentSessionState.getSessionId()).isEqualTo(nextSessionId);
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
        when(mFgSession.getLastEventTimeOffsetSeconds()).thenReturn(lastEventTimeOffset);
        when(mFgSession.getNextReportId()).thenReturn(reportId);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mSessionIDProvider,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory
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
        when(mBgSession.getLastEventTimeOffsetSeconds()).thenReturn(lastEventTimeOffset);
        when(mBgSession.getNextReportId()).thenReturn(reportId);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mSessionIDProvider,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory
        );
        SessionState currentSessionState = mManager.peekCurrentSessionState(mCounterReport);
        assertThat(currentSessionState.getSessionId()).isEqualTo(sessionId);
        assertThat(currentSessionState.getSessionType()).isEqualTo(SessionType.BACKGROUND);
        assertThat(currentSessionState.getReportTime()).isEqualTo(lastEventTimeOffset);
        assertThat(currentSessionState.getReportId()).isEqualTo(reportId);
    }

    @Test
    public void testPeekCurrentSessionStateStateWasNotNull() {
        Session newSession = mock(Session.class);
        long sessionId = 3333333;
        long lastEventTimeOffset = 2222222;
        long reportId = 1111;
        when(newSession.getId()).thenReturn(sessionId);
        when(newSession.getLastEventTimeOffsetSeconds()).thenReturn(lastEventTimeOffset);
        when(newSession.getNextReportId()).thenReturn(reportId);
        when(newSession.getType()).thenReturn(SessionType.FOREGROUND);
        when(mFgSessionFactory.create(any())).thenReturn(newSession);
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            mSessionIDProvider,
            mock(SessionManagerStateMachine.EventSaver.class),
            mFgSessionFactory,
            mBgSessionFactory
        );
        mManager.heartbeat(new CounterReport());
        SessionState currentSessionState = mManager.peekCurrentSessionState(mCounterReport);
        assertThat(currentSessionState.getSessionId()).isEqualTo(sessionId);
        assertThat(currentSessionState.getSessionType()).isEqualTo(SessionType.FOREGROUND);
        assertThat(currentSessionState.getReportTime()).isEqualTo(lastEventTimeOffset);
        assertThat(currentSessionState.getReportId()).isEqualTo(reportId);
    }
}
