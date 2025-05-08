package io.appmetrica.analytics.impl.component.session;

import android.content.Context;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.events.ConditionalEventTrigger;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.ServerTime;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_REGULAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SessionManagerTest extends CommonTest {

    private static final long INITIAL_SESSION_ID = 1000202;

    @Mock
    private PublicLogger publicLogger;
    @Mock
    private SessionIDProvider sessionIDProvider;
    @Mock
    private ExtraMetaInfoRetriever mExtraMetaInfoRetriever;

    private final Context mContext = RuntimeEnvironment.getApplication().getApplicationContext();

    @Mock
    private ComponentUnit mComponentUnit;
    @Mock
    private DatabaseHelper mDbHelper;
    private SessionManagerStateMachine mManager;
    private ISessionFactory mForegroundSessionFactory;
    private ISessionFactory mBackgroundSessionFactory;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mForegroundSessionFactory = new MockSessionFactory(SessionType.FOREGROUND);
        mBackgroundSessionFactory = new MockSessionFactory(SessionType.BACKGROUND);

        when(mComponentUnit.getContext()).thenReturn(mContext);
        when(mComponentUnit.getDbHelper()).thenReturn(mDbHelper);
        when(mComponentUnit.getFreshReportRequestConfig()).thenReturn(mock(ReportRequestConfig.class));
        when(mComponentUnit.getEventTrigger()).thenReturn(mock(ConditionalEventTrigger.class));
        when(mComponentUnit.getVitalComponentDataProvider()).thenReturn(mock(VitalComponentDataProvider.class));
        when(mComponentUnit.getComponentPreferences()).thenReturn(mock(PreferencesComponentDbStorage.class));
        when(mComponentUnit.getPublicLogger()).thenReturn(publicLogger);

        ServerTime.getInstance().init(mock(PreferencesServiceDbStorage.class), mock(TimeProvider.class));
        doReturn(INITIAL_SESSION_ID).when(sessionIDProvider).getNextSessionId();
        mManager = new SessionManagerStateMachine(
            mComponentUnit,
            sessionIDProvider,
            mock(SessionManagerStateMachine.EventSaver.class),
            mForegroundSessionFactory,
            mBackgroundSessionFactory
        );
    }

    @Test
    public void testSessionStateValidForNewForegroundSession() {
        CounterReport regularEvent = new CounterReport();
        regularEvent.setType(EVENT_TYPE_REGULAR.getTypeId());

        mManager.stopCurrentSessionDueToCrash(new CounterReport());
        mManager.heartbeat(regularEvent); //alive report

        SessionState sessionState = mManager.getCurrentSessionState(new CounterReport());

        assertThat(sessionState.getSessionId()).isEqualTo(
            mManager.getSomeSession(new CounterReport()).getId()
        );
        assertThat(sessionState.getSessionType()).isEqualTo(SessionType.FOREGROUND);
        assertThat(sessionState.getReportId()).isEqualTo(1);
        assertThat(sessionState.getReportTime()).isEqualTo(0);
    }

    @Test
    public void testAliveReportForForegroundSessionCreated() throws Exception {
        SessionManagerStateMachine.EventSaver saver = mock(SessionManagerStateMachine.EventSaver.class);
        SessionManagerStateMachine sessionManager = new SessionManagerStateMachine(mComponentUnit, sessionIDProvider, saver);
        sessionManager.heartbeat(new CounterReport());

        sessionManager.stopCurrentSessionDueToCrash(mock(CounterReport.class));
        verify(saver, times(1)).saveEvent(argThat(new ArgumentMatcher<CounterReport>() {
            @Override
            public boolean matches(CounterReport argument) {
                return argument.getType() == InternalEvents.EVENT_TYPE_ALIVE.getTypeId();
            }
        }), any(SessionState.class));
    }

    @Test
    public void testForegroundSessionNotCreatedIfPrevValid() throws Exception {
        ISessionFactory foregroundSessionFactory = spy(new MockSessionFactory(SessionType.FOREGROUND));
        ISessionFactory backgroundSessionFactory = spy(new MockSessionFactory(SessionType.BACKGROUND));
        SessionManagerStateMachine.EventSaver saver = mock(SessionManagerStateMachine.EventSaver.class);

        SessionManagerStateMachine sessionManager = new SessionManagerStateMachine(
            mComponentUnit,
            sessionIDProvider,
            saver,
            foregroundSessionFactory,
            backgroundSessionFactory
        );

        sessionManager.heartbeat(new CounterReport());
        sessionManager.heartbeat(new CounterReport());

        verify(foregroundSessionFactory, times(1)).create(any(SessionArguments.class));
        verify(backgroundSessionFactory, never()).create(any(SessionArguments.class));
    }

    @Test
    public void testAliveReportForForegroundSessionCreatedAfterNewBackground() throws Exception {
        SessionManagerStateMachine.EventSaver saver = mock(SessionManagerStateMachine.EventSaver.class);
        SessionManagerStateMachine sessionManager = new SessionManagerStateMachine(mComponentUnit, sessionIDProvider, saver);

        sessionManager.heartbeat(new CounterReport());
        sessionManager.stopCurrentSessionDueToCrash(new CounterReport());
        sessionManager.getSomeSession(new CounterReport());

        assertThat(sessionManager.getState()).isEqualTo(SessionManagerStateMachine.State.BACKGROUND);
        verify(saver, times(1)).saveEvent(argThat(new ArgumentMatcher<CounterReport>() {
            @Override
            public boolean matches(CounterReport argument) {
                return argument.getType() == InternalEvents.EVENT_TYPE_ALIVE.getTypeId();
            }
        }), any(SessionState.class));
    }

    @Test
    public void testStubForNativeCrash() {
        final long timestamp = 1357986L;
        DatabaseHelper dbHelper = mock(DatabaseHelper.class);
        when(mComponentUnit.getDbHelper()).thenReturn(dbHelper);
        SessionState session = new SessionManagerStateMachine(mComponentUnit, sessionIDProvider,
            mock(SessionManagerStateMachine.EventSaver.class)
        ).createBackgroundSessionStub(timestamp);

        assertThat(session.getSessionId()).isEqualTo(INITIAL_SESSION_ID);
        assertThat(session.getSessionType()).isEqualTo(SessionType.BACKGROUND);
        assertThat(session.getReportId()).isEqualTo(SessionDefaults.INITIAL_REPORT_ID);
        assertThat(session.getReportTime()).isZero();
        verify(dbHelper).newSession(INITIAL_SESSION_ID, SessionType.BACKGROUND, timestamp);

        verify(sessionIDProvider).getNextSessionId();
    }

    @Test
    public void testGetThresholdIdNullSession() {
        assertThat(mManager.getThresholdSessionIdForActualSessions()).isEqualTo(SessionIDProvider.SESSION_ID_MIN_LIMIT);
    }

    @Test
    public void testGetThresholdId() {
        SessionManagerStateMachine manager = new SessionManagerStateMachine(
            mComponentUnit,
            sessionIDProvider,
            mock(SessionManagerStateMachine.EventSaver.class)
        );
        manager.heartbeat(mock(CounterReport.class));
        doReturn(INITIAL_SESSION_ID + 10).when(sessionIDProvider).getNextSessionId();
        manager.heartbeat(mock(CounterReport.class));
        assertThat(manager.getThresholdSessionIdForActualSessions()).isEqualTo(INITIAL_SESSION_ID + 9);
    }

}

