package io.appmetrica.analytics.impl.component;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.AutoCollectedDataSubscribersHolder;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.LifecycleDependentComponentManager;
import io.appmetrica.analytics.impl.ReportingTaskProcessor;
import io.appmetrica.analytics.impl.TaskProcessor;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.impl.component.processor.EventProcessingStrategyFactory;
import io.appmetrica.analytics.impl.component.processor.ReportingReportProcessor;
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine;
import io.appmetrica.analytics.impl.component.session.SessionState;
import io.appmetrica.analytics.impl.component.sessionextras.SessionExtrasHolder;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.impl.events.EventTrigger;
import io.appmetrica.analytics.impl.events.EventTriggerProvider;
import io.appmetrica.analytics.impl.events.EventsFlusher;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.executor.ComponentStartupExecutorFactory;
import io.appmetrica.analytics.impl.startup.executor.StartupExecutor;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ComponentUnitFieldsFactoryTest extends CommonTest {

    Context mContext = RuntimeEnvironment.getApplication();

    @Mock
    ComponentId mComponentId;
    CommonArguments.ReporterArguments mSdkConfig;
    @Mock
    ComponentStartupExecutorFactory mStartupExecutorFactory;
    @Mock
    StartupState mStartupState;
    @Mock
    ReportRequestConfig.DataSendingStrategy dataSendingStrategy;
    @Mock
    ICommonExecutor mExecutor;
    @Mock
    ComponentUnit mComponentUnit;
    @Mock
    PreferencesComponentDbStorage mComponentPreferences;
    @Mock
    LifecycleDependentComponentManager lifecycleDependentComponentManager;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    private EventTriggerProviderCreator eventTriggerProviderCreator;
    @Mock
    private EventsFlusher eventsFlusher;
    @Mock
    private DatabaseHelper databaseHelper;
    @Mock
    private ReportComponentConfigurationHolder configurationHolder;
    @Mock
    private CommonArguments.ReporterArguments initialConfig;
    @Mock
    private ComponentId componentId;
    @Mock
    private PreferencesComponentDbStorage preferencesComponentDbStorage;
    @Mock
    private EventTriggerProvider eventTriggerProvider;
    @Mock
    private EventTrigger eventTrigger;
    @Mock
    private StartupExecutor mStartupExecutor;
    int mCurrentAppVersion = 12;
    private final String apiKey = "some key";

    private ComponentUnitFieldsFactory mFieldsFactory;

    @Rule
    public final GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();
    @Rule
    public final MockedConstructionRule<SessionExtrasHolder> sessionExtrasHolderMockedConstructionRule =
        new MockedConstructionRule<>(SessionExtrasHolder.class);
    @Rule
    public final MockedConstructionRule<AutoCollectedDataSubscribersHolder> autoCollectedDataObserversHolderRule =
        new MockedConstructionRule<>(AutoCollectedDataSubscribersHolder.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mStartupExecutorFactory.create()).thenReturn(mStartupExecutor);
        when(mComponentUnit.getContext()).thenReturn(mContext);
        when(mComponentUnit.getComponentId()).thenReturn(mComponentId);
        when(mComponentUnit.getComponentPreferences()).thenReturn(mComponentPreferences);
        when(mComponentUnit.getStartupState()).thenReturn(TestUtils.createDefaultStartupState());
        when(mComponentId.getApiKey()).thenReturn(apiKey);
        mSdkConfig = CommonArgumentsTestUtils.emptyReporterArguments();
        when(eventTriggerProviderCreator.createEventTriggerProvider(
            eventsFlusher,
            databaseHelper,
            configurationHolder,
            initialConfig,
            componentId,
            preferencesComponentDbStorage
        )).thenReturn(eventTriggerProvider);
        when(eventTriggerProvider.getEventTrigger()).thenReturn(eventTrigger);
        mFieldsFactory = new ComponentUnitFieldsFactory(
            mContext,
            mComponentId,
            mSdkConfig,
            mStartupExecutorFactory,
            mStartupState,
            dataSendingStrategy,
            mExecutor,
            mCurrentAppVersion,
            lifecycleDependentComponentManager,
            eventTriggerProviderCreator
        );
    }

    @Test
    public void testGetLoggerProvider() {
        assertThat(mFieldsFactory.getLoggerProvider()).isNotNull();
    }

    @Test
    public void testGetPreferencesProvider() {
        assertThat(mFieldsFactory.getPreferencesProvider()).isNotNull();
    }

    @Test
    public void testCreateEventFirstOccurrenceService() {
        assertThat(mFieldsFactory.createEventFirstOccurrenceService()).isNotNull();
    }

    @Test
    public void testCreateDatabaseHelper() {
        assertThat(mFieldsFactory.createDatabaseHelper(mComponentUnit)).isNotNull();
    }

    @Test
    public void testCreateTaskProcessor() {
        TaskProcessor taskProcessor = mFieldsFactory.createTaskProcessor(mComponentUnit);
        assertThat(taskProcessor).isNotNull();
        verify(mStartupExecutorFactory).create();
        verify(lifecycleDependentComponentManager).addLifecycleObserver(taskProcessor);
    }

    @Test
    public void testCreateConfigHolder() {
        assertThat(mFieldsFactory.createConfigHolder(mComponentUnit)).isNotNull()
            .isExactlyInstanceOf(ReportComponentConfigurationHolder.class);
    }

    @Test
    public void testCreateSessionManager() {
        SessionManagerStateMachine.EventSaver saver = mock(SessionManagerStateMachine.EventSaver.class);
        SessionManagerStateMachine sessionManager = mFieldsFactory.createSessionManager(mComponentUnit, vitalComponentDataProvider, saver);
        assertThat(sessionManager.getSaver()).isEqualTo(saver);
    }

    @Test
    public void testCreateReportSaver() {
        ReportingTaskProcessor taskProcessor = mock(ReportingTaskProcessor.class);
        EventSaver eventSaver = mFieldsFactory.createReportSaver(
            mComponentPreferences,
            vitalComponentDataProvider,
            mock(SessionManagerStateMachine.class),
            mock(DatabaseHelper.class), mock(AppEnvironment.class),
            mock(SessionExtrasHolder.class),
            taskProcessor
        );
        eventSaver.saveReport(mock(CounterReport.class), mock(SessionState.class));
        verify(taskProcessor).restartFlushTask();
    }

    @Test
    public void testCreateEventProcessingStrategyFactory() {
        assertThat(mFieldsFactory.createEventProcessingStrategyFactory(mComponentUnit)).isNotNull();
    }

    @Test
    public void testCreateReportProcessor() {
        assertThat(mFieldsFactory.createReportProcessor(mComponentUnit, mock(EventProcessingStrategyFactory.class)))
            .isNotNull().isExactlyInstanceOf(ReportingReportProcessor.class);
    }

    @Test
    public void testCreateMigrationHelperCreator() {
        assertThat(mFieldsFactory.createMigrationHelperCreator(mComponentUnit)).isNotNull();
    }

    @Test
    public void testCreateEventTrigger() {
        assertThat(mFieldsFactory.createEventTrigger(
            eventsFlusher,
            databaseHelper,
            configurationHolder,
            initialConfig,
            componentId,
            preferencesComponentDbStorage
        )).isEqualTo(eventTrigger);
    }

    @Test
    public void testCreateCertificateFingerprintProvider() {
        assertThat(mFieldsFactory.createCertificateFingerprintProvider(mComponentPreferences)).isNotNull();
    }

    @Test
    public void getVitalComponentDataProvider() {
        when(GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getComponentDataProvider(mComponentId))
            .thenReturn(vitalComponentDataProvider);
        assertThat(mFieldsFactory.getVitalComponentDataProvider()).isSameAs(vitalComponentDataProvider);
    }

    @Test
    public void createSessionExtrasHolder() {
        SessionExtrasHolder first = mFieldsFactory.createSessionExtraHolder();
        SessionExtrasHolder seconds = mFieldsFactory.createSessionExtraHolder();

        assertThat(sessionExtrasHolderMockedConstructionRule.getConstructionMock().constructed()).hasSize(2);
        assertThat(first)
            .isEqualTo(sessionExtrasHolderMockedConstructionRule.getConstructionMock().constructed().get(0));
        assertThat(seconds)
            .isEqualTo(sessionExtrasHolderMockedConstructionRule.getConstructionMock().constructed().get(1));
        assertThat(sessionExtrasHolderMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(mContext, mComponentId, mContext, mComponentId);
    }

    @Test
    public void getAutoCollectedDataSubscribersHolder() {
        assertThat(mFieldsFactory.createAutoCollectedDataSubscribersHolder(mComponentPreferences))
            .isEqualTo(autoCollectedDataObserversHolderRule.getConstructionMock().constructed().get(0));

        assertThat(autoCollectedDataObserversHolderRule.getConstructionMock().constructed())
            .hasSize(1);
        assertThat(autoCollectedDataObserversHolderRule.getArgumentInterceptor().flatArguments())
            .containsExactly(mComponentId, mComponentPreferences);
    }

    @RunWith(RobolectricTestRunner.class)
    public static class LoggerProviderTest {

        private final String mApiKey = TestsData.generateApiKey();
        private final ComponentUnitFieldsFactory.LoggerProvider mLoggerProvdier = new ComponentUnitFieldsFactory.LoggerProvider(mApiKey);

        @Test
        public void testGetPublicLogger() {
            assertThat(mLoggerProvdier.getPublicLogger()).isNotNull().isExactlyInstanceOf(PublicLogger.class);
        }
    }

    @RunWith(RobolectricTestRunner.class)
    public static class PreferencesProviderTest {

        @Mock
        private ComponentId componentId;
        @Mock
        private DatabaseStorageFactory storageFactory;
        @Mock
        private IKeyValueTableDbHelper keyValueTableDbHelper;
        private ComponentUnitFieldsFactory.PreferencesProvider preferencesProvider;

        @Before
        public void setUp() {
            MockitoAnnotations.openMocks(this);
            when(storageFactory.getPreferencesDbHelper(componentId)).thenReturn(keyValueTableDbHelper);
            preferencesProvider = new ComponentUnitFieldsFactory.PreferencesProvider(componentId, storageFactory);
        }

        @Test
        public void testGetComponentPreferences() {
            assertThat(preferencesProvider.createPreferencesComponentDbStorage()).isNotNull().isExactlyInstanceOf(PreferencesComponentDbStorage.class);
            verify(storageFactory).getPreferencesDbHelper(componentId);
        }
    }
}
