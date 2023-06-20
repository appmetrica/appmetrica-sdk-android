package io.appmetrica.analytics.impl.component.session;

import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Random;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SessionFactoryTest extends CommonTest {

    boolean mDebugBackup = BuildConfig.DEBUG;

    @Mock
    private ComponentUnit mComponent;

    private SessionStorageImpl mSessionStorage;
    @Mock
    private SessionArguments mSessionArguments;
    @Mock
    private DatabaseHelper mDatabaseHelper;
    @Mock
    private PreferencesComponentDbStorage preferencesComponentDbStorage;
    @Mock
    private SessionIDProvider sessionIDProvider;
    @Mock
    private IReporterExtended mSelfReporter;
    @Mock
    private SystemTimeProvider timeProvider;

    private AbstractSessionFactory mFactory;
    private String mSessionState;

    private static final String SESSION_TAG = "Tag";

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.openMocks(this);
        mDebugBackup = BuildConfig.DEBUG;

        mSessionStorage = new SessionStorageImpl(preferencesComponentDbStorage, SESSION_TAG);

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                mSessionState = (String) invocation.getArguments()[0];
                return preferencesComponentDbStorage;
            }
        }).when(preferencesComponentDbStorage).putSessionParameters(eq(SESSION_TAG), anyString());

        when(preferencesComponentDbStorage.getSessionParameters(SESSION_TAG)).thenReturn(mSessionState);

        when(mComponent.getDbHelper()).thenReturn(mDatabaseHelper);

        mFactory = new AbstractSessionFactory(
                mComponent,
                sessionIDProvider,
                mSessionStorage,
                SessionFactoryArguments.newBuilder(SessionType.FOREGROUND).build(),
                mSelfReporter,
                timeProvider
        ) {
        };
    }

    @Test
    public void testCreateNewSessionCreateFirstSessionWithExpectedSessionId() {
        Session session = mFactory.create(mSessionArguments);
        verify(sessionIDProvider).getNextSessionId();
    }

    @Test
    public void testSessionCreatedIfHasNotValues() {
        assertThat(factoryWithoutValues().create(mock(SessionArguments.class))).isNotNull();
    }

    @Test
    public void testSessionLoaded() {
        assertThat(factoryWithValues().load()).isNotNull();
    }

    @Test
    public void testSessionNotLoadedWithoutValues() {
        assertThat(factoryWithoutValues().load()).isNull();
    }

    @Test
    public void testBackgroundSessionFactory() {
        AbstractSessionFactory factory = factoryWithValues(new FactoryFactory() {
            public AbstractSessionFactory createFactory(ComponentUnit componentUnit,
                                                        SessionIDProvider sessionIDProvider,
                                                        SessionStorageImpl sessionStorage,
                                                        SessionFactoryArguments arguments,
                                                        IReporterExtended selfReporter) {
                return new BackgroundSessionFactory(componentUnit, sessionIDProvider, sessionStorage, selfReporter, timeProvider);
            }
        }, false);
        Session session = factory.create(mock(SessionArguments.class));
        assertThat(session.getType()).isEqualTo(SessionType.BACKGROUND);
        assertThat(session.getTimeout()).isEqualTo(60 * 60);
    }

    @Test
    public void testForegroundSessionFactory() {
        AbstractSessionFactory factory = factoryWithValues(new FactoryFactory() {
            public AbstractSessionFactory createFactory(ComponentUnit componentUnit,
                                                        SessionIDProvider sessionIDProvider,
                                                        SessionStorageImpl sessionStorage,
                                                        SessionFactoryArguments arguments,
                                                        IReporterExtended selfReporter) {
                return new ForegroundSessionFactory(componentUnit, sessionIDProvider, sessionStorage, selfReporter, timeProvider);
            }
        }, false);
        Session session = factory.create(mock(SessionArguments.class));
        assertThat(session.getType()).isEqualTo(SessionType.FOREGROUND);
        assertThat(session.getTimeout()).isEqualTo(10);
    }

    @Test
    public void testFillArguments() {
        SessionStorageImpl sessionStorage = new SessionStorageImpl(mock(PreferencesComponentDbStorage.class), "tag");
        Random random = new Random();
        boolean aliveNeeded = true;
        int sleepStart = random.nextInt(1000);
        int creationTime = random.nextInt(1000) + 1000;
        int reportId = random.nextInt(1000) + 2000;
        int sessionId = random.nextInt(1000) + 3000;
        int sessionTimeout = random.nextInt(1000) + 4000;
        sessionStorage.putAliveReportNeeded(aliveNeeded)
                .putSleepStart(sleepStart)
                .putCreationTime(creationTime)
                .putReportId(reportId)
                .putSessionId(sessionId)
                .commit();
        SessionArgumentsInternal arguments = new AbstractSessionFactory(
                mock(ComponentUnit.class),
                sessionIDProvider,
                sessionStorage,
                SessionFactoryArguments.newBuilder(SessionType.FOREGROUND).withSessionTimeout(sessionTimeout).build(),
                mSelfReporter,
                timeProvider
        ) {
        }.fillFromStorage();
        assertThat(arguments.isAliveNeeded(false)).isTrue();
        assertThat(arguments.getSleepStart(-1)).isEqualTo(sleepStart);
        assertThat(arguments.getCreationTime(-1)).isEqualTo(creationTime);
        assertThat(arguments.getCurrentReportId(-1)).isEqualTo(reportId);
        assertThat(arguments.getId(-1)).isEqualTo(sessionId);
        assertThat(arguments.getTimeout(-1)).isEqualTo(sessionTimeout);
        assertThat(arguments.getType()).isEqualTo(SessionType.FOREGROUND);
    }

    private AbstractSessionFactory factoryWithValues() {
        return factoryWithValues(new StubbedFactory(), true);
    }

    private AbstractSessionFactory factoryWithoutValues() {
        return factoryWithValues(new StubbedFactory(), false);
    }

    private AbstractSessionFactory factoryWithValues(FactoryFactory factoryFactory, boolean hasValues) {
        SessionStorageImpl sessionStorage = spy(new SessionStorageImpl(
                mock(PreferencesComponentDbStorage.class),
                "tag"
        ));
        ComponentUnit componentUnit = mock(ComponentUnit.class);
        ReportRequestConfig requestConfig = mock(ReportRequestConfig.class);
        doReturn(10).when(requestConfig).getSessionTimeout();
        doReturn(requestConfig).when(componentUnit).getFreshReportRequestConfig();
        doReturn(RuntimeEnvironment.getApplication()).when(componentUnit).getContext();
        doReturn(mock(DatabaseHelper.class)).when(componentUnit).getDbHelper();
        AbstractSessionFactory factory = factoryFactory.createFactory(
                componentUnit,
                sessionIDProvider,
                sessionStorage,
                SessionFactoryArguments.newBuilder(SessionType.FOREGROUND).build(),
                mSelfReporter
        );
        doReturn(hasValues).when(sessionStorage).hasValues();
        return factory;
    }

    private static final class StubbedFactory implements FactoryFactory {

        public AbstractSessionFactory createFactory(ComponentUnit componentUnit,
                                                    SessionIDProvider sessionIDProvider,
                                                    SessionStorageImpl sessionStorage,
                                                    SessionFactoryArguments arguments,
                                                    IReporterExtended selfReporter) {
            return new AbstractSessionFactory(
                    componentUnit,
                    sessionIDProvider,
                    sessionStorage,
                    arguments,
                    selfReporter,
                    mock(SystemTimeProvider.class)
            ) {
            };
        }
    }

    private interface FactoryFactory {

        AbstractSessionFactory createFactory(ComponentUnit componentUnit,
                                             SessionIDProvider sessionIDProvider,
                                             SessionStorageImpl sessionStorage,
                                             SessionFactoryArguments arguments,
                                             IReporterExtended selfReporter);

    }

}
