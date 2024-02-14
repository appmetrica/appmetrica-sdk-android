package io.appmetrica.analytics.impl.component;

import android.content.Context;
import android.util.Pair;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.CertificatesFingerprintsProvider;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.PreloadInfoStorage;
import io.appmetrica.analytics.impl.ReportingTaskProcessor;
import io.appmetrica.analytics.impl.component.processor.EventProcessingStrategyFactory;
import io.appmetrica.analytics.impl.component.processor.ReportingReportProcessor;
import io.appmetrica.analytics.impl.component.remarketing.EventFirstOccurrenceService;
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine;
import io.appmetrica.analytics.impl.component.sessionextras.SessionExtrasHolder;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.events.ContainsUrgentEventsCondition;
import io.appmetrica.analytics.impl.events.EventTrigger;
import io.appmetrica.analytics.impl.events.EventsFlusher;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class ComponentUnitBaseTest extends CommonTest {

    private final String mNeedToCheckPermissionsTag = "need to check permissions";
    private final String mForceSendPermissionsTag = "should force send permissions";
    Context mContext;
    @Mock
    private StartupState mStartupState;
    @Mock
    ComponentId mComponentId;
    @Mock
    private CommonArguments.ReporterArguments mReporterArguments;
    @Mock
    AppEnvironmentProvider mAppEnvironmentProvider;
    @Mock
    private AppEnvironment mAppEnvironment;
    @Mock
    private PublicLogger mPublicLogger;
    @Mock
    private CounterReport mCounterReport;
    @Mock
    private ComponentUnitFieldsFactory.LoggerProvider mLoggerProvider;
    @Mock
    private ComponentUnitFieldsFactory.PreferencesProvider mPreferencesProvider;
    @Mock
    PreferencesComponentDbStorage mComponentPreferences;
    @Mock
    private PreferencesServiceDbStorage mServicePreferences;
    @Mock
    private EventFirstOccurrenceService mEventFirstOccurrenceService;
    @Mock
    private DatabaseHelper mDatabaseHelper;
    @Mock
    private ReportingTaskProcessor mTaskProcessor;
    @Mock
    ReportComponentConfigurationHolder mConfigHolder;
    @Mock
    private SessionManagerStateMachine mSessionManager;
    @Mock
    private EventSaver mEventSaver;
    @Mock
    EventProcessingStrategyFactory mEventProcessingStrategyFactory;
    @Mock
    private ReportingReportProcessor mReportProcessor;
    @Mock
    private ComponentMigrationHelper.Creator mMigrationHelperCreator;
    @Mock
    private ComponentMigrationHelper mMigrationHelper;
    @Mock
    private ReportRequestConfig mReportRequestConfig;
    @Mock
    private EventTrigger mEventTrigger;
    @Mock
    private ContainsUrgentEventsCondition mUrgentEventsCondition;
    @Mock
    private CertificatesFingerprintsProvider mCertificatesFingerprintsProvider;
    @Mock
    TimePassedChecker mTimePassedChecker;
    @Mock
    PreloadInfoStorage mPreloadInfoStorage;
    @Mock
    VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    SessionExtrasHolder sessionExtrasHolder;
    private AppEnvironment.EnvironmentRevision mRevision;

    final String mApiKey = UUID.randomUUID().toString();
    private ComponentUnitFieldsFactory mFieldsFactory;
    protected ComponentUnit mComponentUnit;

    public void init() {
        MockitoAnnotations.openMocks(this);
        mFieldsFactory = createFieldsFactory();
        mContext = RuntimeEnvironment.getApplication();
        mRevision = new AppEnvironment.EnvironmentRevision("", 0);
        when(mConfigHolder.get()).thenReturn(mReportRequestConfig);
        when(mComponentId.getApiKey()).thenReturn(mApiKey);
        when(mFieldsFactory.getLoggerProvider()).thenReturn(mLoggerProvider);
        when(mFieldsFactory.getPreferencesProvider()).thenReturn(mPreferencesProvider);
        when(mLoggerProvider.getPublicLogger()).thenReturn(mPublicLogger);
        when(mPreferencesProvider.createPreferencesComponentDbStorage()).thenReturn(mComponentPreferences);
        when(mAppEnvironmentProvider.getOrCreate(mComponentId, mPublicLogger, mComponentPreferences)).thenReturn(mAppEnvironment);
        when(mFieldsFactory.createEventFirstOccurrenceService()).thenReturn(mEventFirstOccurrenceService);
        when(mFieldsFactory.createDatabaseHelper(any(ComponentUnit.class))).thenReturn(mDatabaseHelper);
        when(mFieldsFactory.createTaskProcessor(any(ComponentUnit.class))).thenReturn(mTaskProcessor);
        when(mFieldsFactory.createConfigHolder(any(ComponentUnit.class))).thenReturn(mConfigHolder);
        when(mFieldsFactory.createSessionManager(any(ComponentUnit.class), same(vitalComponentDataProvider), any(SessionManagerStateMachine.EventSaver.class))).thenReturn(mSessionManager);
        when(createFieldsFactory().createReportSaver(
            mComponentPreferences,
            vitalComponentDataProvider,
            mSessionManager, mDatabaseHelper,
            mAppEnvironment,
            sessionExtrasHolder,
            mTaskProcessor
        )).thenReturn(mEventSaver);
        when(mFieldsFactory.createEventProcessingStrategyFactory(any(ComponentUnit.class))).thenReturn(mEventProcessingStrategyFactory);
        when(mFieldsFactory.createReportProcessor(any(ComponentUnit.class), same(mEventProcessingStrategyFactory))).thenReturn(mReportProcessor);
        when(mFieldsFactory.createMigrationHelperCreator(any(ComponentUnit.class))).thenReturn(mMigrationHelperCreator);
        when(mFieldsFactory.createUrgentEventsCondition(mDatabaseHelper)).thenReturn(mUrgentEventsCondition);
        when(mFieldsFactory.createEventTrigger(any(List.class), any(EventsFlusher.class))).thenReturn(mEventTrigger);
        when(mFieldsFactory.createCertificateFingerprintProvider(mComponentPreferences)).thenReturn(mCertificatesFingerprintsProvider);
        when(mFieldsFactory.getVitalComponentDataProvider()).thenReturn(vitalComponentDataProvider);
        when(mFieldsFactory.createSessionExtraHolder()).thenReturn(sessionExtrasHolder);
        when(mMigrationHelperCreator.create(any(ComponentUnit.class))).thenReturn(mMigrationHelper);
        when(mAppEnvironment.getLastRevision()).thenReturn(mRevision);
        when(vitalComponentDataProvider.getLastMigrationApiLevel()).thenReturn(AppMetrica.getLibraryApiLevel());
        when(vitalComponentDataProvider.getLastMigrationApiLevel()).thenReturn(AppMetrica.getLibraryApiLevel());
        when(GlobalServiceLocator.getInstance().getPreloadInfoStorage()).thenReturn(mPreloadInfoStorage);
        when(GlobalServiceLocator.getInstance().getServicePreferences()).thenReturn(mServicePreferences);
        initCustomFields();
        mComponentUnit = createComponentUnit();
    }

    protected abstract void initCustomFields();

    protected abstract ComponentUnit createComponentUnit();

    protected abstract ComponentUnitFieldsFactory createFieldsFactory();

    @Test
    public void testDatabaseHelperNotified() {
        verify(mDatabaseHelper).onComponentCreated();
    }

    @Test
    public void testPreferencesMigratedHasOldApiLevelShouldMigrate() {
        int apiLevel = AppMetrica.getLibraryApiLevel();
        when(mPreferencesProvider.createPreferencesComponentDbStorage()).thenReturn(mComponentPreferences);
        when(vitalComponentDataProvider.getLastMigrationApiLevel()).thenReturn(apiLevel - 1);
        ComponentMigrationHelper.Creator creator = mock(ComponentMigrationHelper.Creator.class);
        ComponentMigrationHelper migrationHelper = mock(ComponentMigrationHelper.class);
        when(mFieldsFactory.createMigrationHelperCreator(any(ComponentUnit.class))).thenReturn(creator);
        when(creator.create(any(ComponentUnit.class))).thenReturn(migrationHelper);
        mComponentUnit = createComponentUnit();
        verify(migrationHelper).migrate(apiLevel - 1);
        verify(vitalComponentDataProvider).setLastMigrationApiLevel(apiLevel);
    }

    @Test
    public void testPreferencesMigratedShouldNotMigrate() {
        int apiLevel = AppMetrica.getLibraryApiLevel();
        when(mPreferencesProvider.createPreferencesComponentDbStorage()).thenReturn(mComponentPreferences);
        when(vitalComponentDataProvider.getLastMigrationApiLevel()).thenReturn(apiLevel);
        ComponentMigrationHelper.Creator creator = mock(ComponentMigrationHelper.Creator.class);
        ComponentMigrationHelper migrationHelper = mock(ComponentMigrationHelper.class);
        when(mFieldsFactory.createMigrationHelperCreator(any(ComponentUnit.class))).thenReturn(creator);
        when(creator.create(mComponentUnit)).thenReturn(migrationHelper);
        mComponentUnit = createComponentUnit();
        verify(migrationHelper, never()).migrate(apiLevel);
        verify(vitalComponentDataProvider, never()).setLastMigrationApiLevel(apiLevel);
    }

    @Test
    public void testPreferencesMigratedDoesNotHaveOldApiLevelShouldMigrate() {
        int apiLevel = AppMetrica.getLibraryApiLevel();
        when(mPreferencesProvider.createPreferencesComponentDbStorage()).thenReturn(mComponentPreferences);
        when(vitalComponentDataProvider.getLastMigrationApiLevel()).thenReturn(apiLevel - 1);
        ComponentMigrationHelper.Creator creator = mock(ComponentMigrationHelper.Creator.class);
        ComponentMigrationHelper migrationHelper = mock(ComponentMigrationHelper.class);
        when(mFieldsFactory.createMigrationHelperCreator(any(ComponentUnit.class))).thenReturn(creator);
        when(creator.create(any(ComponentUnit.class))).thenReturn(migrationHelper);
        mComponentUnit = createComponentUnit();
        verify(migrationHelper).migrate(apiLevel - 1);
        verify(vitalComponentDataProvider).setLastMigrationApiLevel(apiLevel);
    }

    @Test
    public void testPreferencesMigratedDoesNotHaveOldApiLevelShouldNotMigrate() {
        int apiLevel = AppMetrica.getLibraryApiLevel();
        when(mPreferencesProvider.createPreferencesComponentDbStorage()).thenReturn(mComponentPreferences);
        when(vitalComponentDataProvider.getLastMigrationApiLevel()).thenReturn(apiLevel);
        ComponentMigrationHelper.Creator creator = mock(ComponentMigrationHelper.Creator.class);
        ComponentMigrationHelper migrationHelper = mock(ComponentMigrationHelper.class);
        when(mFieldsFactory.createMigrationHelperCreator(any(ComponentUnit.class))).thenReturn(creator);
        when(creator.create(any(ComponentUnit.class))).thenReturn(migrationHelper);
        mComponentUnit = createComponentUnit();
        verify(migrationHelper, never()).migrate(apiLevel);
        verify(vitalComponentDataProvider, never()).setLastMigrationApiLevel(apiLevel);
    }

    @Test
    public void testEventLoggedIfLoggerEnabled() {
        when(mPublicLogger.isEnabled()).thenReturn(true);
        mComponentUnit.handleReport(mCounterReport);
        verify(mPublicLogger).logEvent(mCounterReport, "Event received on service");
    }

    @Test
    public void testEventNotLoggedIfLoggerDisabled() {
        when(mPublicLogger.isEnabled()).thenReturn(false);
        mComponentUnit.handleReport(mCounterReport);
        verify(mPublicLogger, never()).logEvent(any(CounterReport.class), anyString());
    }

    @Test
    public void testGetSessionManager() {
        assertThat(mComponentUnit.getSessionManager()).isEqualTo(mSessionManager);
    }

    @Test
    public void testGetReportSaver() {
        assertThat(mComponentUnit.getEventSaver()).isEqualTo(mEventSaver);
    }

    @Test
    public void testGetStartupState() {
        when(mConfigHolder.getStartupState()).thenReturn(mStartupState);
        assertThat(mComponentUnit.getStartupState()).isEqualTo(mStartupState);
    }

    @Test
    public void testGetEventProcessingStrategyFactory() {
        assertThat(mComponentUnit.getEventProcessingStrategyFactory()).isEqualTo(mEventProcessingStrategyFactory);
    }

    @Test
    public void testHandleReport() {
        mComponentUnit.handleReport(mCounterReport);
        verify(mReportProcessor).process(mCounterReport);
    }

    @Test
    public void testUpdateSdkConfig() {
        mComponentUnit.updateSdkConfig(mReporterArguments);
        verify(mConfigHolder).updateArguments(mReporterArguments);
    }

    @Test
    public void testLoggerStateUpdatedTrue() {
        CommonArguments arguments = new CommonArguments(
                mock(StartupRequestConfig.Arguments.class),
                createReporterArgumentsWithLogsEnabled(true),
                null
        );
        mComponentUnit.updateSdkConfig(arguments.componentArguments);
        verify(mPublicLogger).setEnabled(true);
    }

    @Test
    public void testLoggerStateUpdatedFalse() {
        CommonArguments arguments = new CommonArguments(
                mock(StartupRequestConfig.Arguments.class),
                createReporterArgumentsWithLogsEnabled(false),
                null
        );
        mComponentUnit.updateSdkConfig(arguments.componentArguments);
        verify(mPublicLogger).setEnabled(false);
    }

    @Test
    public void testLoggerStateUpdatedNull() {
        CommonArguments arguments = new CommonArguments(
                mock(StartupRequestConfig.Arguments.class),
                createReporterArgumentsWithLogsEnabled(null),
                null
        );
        mComponentUnit.updateSdkConfig(arguments.componentArguments);
        verify(mPublicLogger, never()).setEnabled(ArgumentMatchers.nullable(Boolean.class));
    }

    @Test
    public void testFlushEvents() {
        mComponentUnit.flushEvents();
        verify(mTaskProcessor).flushAllTasks();
    }

    @Test
    public void testGetFreshReportConfig() {
        ReportRequestConfig requestConfig = mock(ReportRequestConfig.class);
        when(mConfigHolder.get()).thenReturn(requestConfig);
        assertThat(mComponentUnit.getFreshReportRequestConfig()).isEqualTo(requestConfig);
    }

    @Test
    public void testGetDbHelper() {
        assertThat(mComponentUnit.getDbHelper()).isEqualTo(mDatabaseHelper);
    }

    @Test
    public void testGetComponentId() {
        assertThat(mComponentUnit.getComponentId()).isEqualTo(mComponentId);
    }

    @Test
    public void testGetEventsTrigger() {
        assertThat(mComponentUnit.getEventTrigger()).isEqualTo(mEventTrigger);
    }

    @Test
    public void testGetReportsListener() {
        assertThat(mComponentUnit.getReportsListener()).isEqualTo(mUrgentEventsCondition);
    }

    @Test
    public void getServicePreferences() {
        assertThat(mComponentUnit.getServicePreferences()).isSameAs(mServicePreferences);
    }

    @Test
    public void testOnStartupChanged() {
        StartupState startupState = TestUtils.createDefaultStartupStateBuilder().build();
        mComponentUnit.onStartupChanged(startupState);
        verify(mConfigHolder).updateStartupState(startupState);
        verify(mEventTrigger).trigger();
    }

    @Test
    public void testGetContext() {
        assertThat(mComponentUnit.getContext()).isEqualTo(mContext);
    }

    @Test
    public void testGetPublicLogger() {
        assertThat(mComponentUnit.getPublicLogger()).isEqualTo(mPublicLogger);
    }

    @Test
    public void testMarkPermissionsChecked() {
        mComponentUnit.markFeaturesChecked();
        verify(mEventSaver).saveFeaturesCheckVersion();
    }

    @Test
    public void testMarkFeaturesChecked() {
        mComponentUnit.markFeaturesChecked();
        verify(mEventSaver).saveFeaturesCheckVersion();
    }

    @Test
    public void testAddAppEnvironmentValue() {
        Pair<String, String> environment = new Pair<String, String>("aaa", "bbb");
        when(mCounterReport.getAppEnvironment()).thenReturn(environment);
        mComponentUnit.addAppEnvironmentValue(mCounterReport);
        verify(mAppEnvironment).add(environment);
        verify(mAppEnvironmentProvider).commitIfNeeded(mRevision, mComponentPreferences);
    }

    @Test
    public void testClearAppEnvironment() {
        AppEnvironment.EnvironmentRevision revision = new AppEnvironment.EnvironmentRevision("value", 10);
        when(mAppEnvironment.getLastRevision()).thenReturn(revision);
        mComponentUnit.clearAppEnvironment();
        verify(mAppEnvironment).reset();
        verify(mAppEnvironmentProvider).commit(revision, mComponentPreferences);
    }

    @Test
    public void resetConfigHolder() {
        mComponentUnit.resetConfigHolder();
        verify(mConfigHolder).reset();
    }

    @Test
    public void testNeedToCheckPermissionsEventNotLongAgoCollectingDisabledStartupNotValid() {
        final long interval = 500;
        final long permissionsCheckTime = 44442222;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mNeedToCheckPermissionsTag)).thenReturn(false);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(false);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(false);
        when(mReportRequestConfig.getPermissionsCollectingIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.needToCheckPermissions()).isFalse();
    }

    @Test
    public void testNeedToCheckPermissionsEventNotLongAgoCollectingDisabledStartupValid() {
        final long permissionsCheckTime = 44442222;
        final long interval = 500;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mNeedToCheckPermissionsTag)).thenReturn(false);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(false);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(true);
        when(mReportRequestConfig.getPermissionsCollectingIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.needToCheckPermissions()).isFalse();
    }

    @Test
    public void testNeedToCheckPermissionsEventNotLongAgoCollectingEnabledStartupNotValid() {
        final long permissionsCheckTime = 44442222;
        final long interval = 500;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mNeedToCheckPermissionsTag)).thenReturn(false);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(true);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(false);
        when(mReportRequestConfig.getPermissionsCollectingIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.needToCheckPermissions()).isFalse();
    }

    @Test
    public void testNeedToCheckPermissionsEventNotLongAgoCollectingEnabledStartupValid() {
        final long permissionsCheckTime = 44442222;
        final long interval = 500;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mNeedToCheckPermissionsTag)).thenReturn(false);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(true);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(true);
        when(mReportRequestConfig.getPermissionsCollectingIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.needToCheckPermissions()).isFalse();
    }

    @Test
    public void testNeedToCheckPermissionsEventLongAgoCollectingDisabledStartupNotValid() {
        final long permissionsCheckTime = 44442222;
        final long interval = 500;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mNeedToCheckPermissionsTag)).thenReturn(true);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(false);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(false);
        when(mReportRequestConfig.getPermissionsCollectingIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.needToCheckPermissions()).isFalse();
    }

    @Test
    public void testNeedToCheckPermissionsEventLongAgoCollectingDisabledStartupValid() {
        final long permissionsCheckTime = 44442222;
        final long interval = 500;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mNeedToCheckPermissionsTag)).thenReturn(true);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(false);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(true);
        when(mReportRequestConfig.getPermissionsCollectingIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.needToCheckPermissions()).isFalse();
    }

    @Test
    public void testNeedToCheckPermissionsEventLongAgoCollectingEnabledStartupNotValid() {
        final long permissionsCheckTime = 44442222;
        final long interval = 500;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mNeedToCheckPermissionsTag)).thenReturn(true);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(true);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(false);
        when(mReportRequestConfig.getPermissionsCollectingIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.needToCheckPermissions()).isFalse();
    }

    @Test
    public void testNeedToCheckPermissionsEventLongAgoCollectingEnabledStartupValid() {
        final long permissionsCheckTime = 44442222;
        final long interval = 500;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mNeedToCheckPermissionsTag)).thenReturn(true);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(true);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(true);
        when(mReportRequestConfig.getPermissionsCollectingIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.needToCheckPermissions()).isTrue();
    }

    @Test
    public void testShouldForceSendPermissionsWasLongAgoAndEnabled() {
        final long permissionsCheckTime = 44442222;
        final long interval = 500;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mForceSendPermissionsTag)).thenReturn(true);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(true);
        when(mReportRequestConfig.getPermissionsForceSendIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.shouldForceSendPermissions()).isTrue();
    }

    @Test
    public void testShouldForceSendPermissionsWasLongAgoAndDisabled() {
        final long permissionsCheckTime = 44442222;
        final long interval = 500;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mForceSendPermissionsTag)).thenReturn(true);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(false);
        when(mReportRequestConfig.getPermissionsForceSendIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.shouldForceSendPermissions()).isFalse();
    }

    @Test
    public void testShouldForceSendPermissionsWasNotLongAgoAndDisabled() {
        final long permissionsCheckTime = 44442222;
        final long interval = 500;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mForceSendPermissionsTag)).thenReturn(false);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(false);
        when(mReportRequestConfig.getPermissionsForceSendIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.shouldForceSendPermissions()).isFalse();
    }

    @Test
    public void testShouldForceSendPermissionsWasNotLongAgoAndEnabled() {
        final long permissionsCheckTime = 44442222;
        final long interval = 500;
        when(mTimePassedChecker.didTimePassSeconds(permissionsCheckTime, interval, mForceSendPermissionsTag)).thenReturn(false);
        when(mEventSaver.getPermissionsCheckTime()).thenReturn(permissionsCheckTime);
        when(mReportRequestConfig.isPermissionsCollectingEnabled()).thenReturn(true);
        when(mReportRequestConfig.getPermissionsForceSendIntervalSeconds()).thenReturn(interval);
        assertThat(mComponentUnit.shouldForceSendPermissions()).isFalse();
    }

    @Test
    public void testNeedToCollectFeaturesEventNotLongAgoCollectingDisabledStartupNotValid() {
        when(mEventSaver.wasLastFeaturesEventLongAgo()).thenReturn(false);
        when(mReportRequestConfig.isFeaturesCollectingEnabled()).thenReturn(false);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(false);
        assertThat(mComponentUnit.needToCollectFeatures()).isFalse();
    }

    @Test
    public void testNeedToCollectFeaturesEventNotLongAgoCollectingDisabledStartupValid() {
        when(mEventSaver.wasLastFeaturesEventLongAgo()).thenReturn(false);
        when(mReportRequestConfig.isFeaturesCollectingEnabled()).thenReturn(false);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(true);
        assertThat(mComponentUnit.needToCollectFeatures()).isFalse();
    }

    @Test
    public void testNeedToCollectFeaturesEventNotLongAgoCollectingEnabledStartupNotValid() {
        when(mEventSaver.wasLastFeaturesEventLongAgo()).thenReturn(false);
        when(mReportRequestConfig.isFeaturesCollectingEnabled()).thenReturn(true);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(false);
        assertThat(mComponentUnit.needToCollectFeatures()).isFalse();
    }

    @Test
    public void testNeedToCollectFeaturesEventNotLongAgoCollectingEnabledStartupValid() {
        when(mEventSaver.wasLastFeaturesEventLongAgo()).thenReturn(false);
        when(mReportRequestConfig.isFeaturesCollectingEnabled()).thenReturn(true);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(true);
        assertThat(mComponentUnit.needToCollectFeatures()).isFalse();
    }

    @Test
    public void testNeedToCollectFeaturesEventLongAgoCollectingDisabledStartupNotValid() {
        when(mEventSaver.wasLastFeaturesEventLongAgo()).thenReturn(true);
        when(mReportRequestConfig.isFeaturesCollectingEnabled()).thenReturn(false);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(false);
        assertThat(mComponentUnit.needToCollectFeatures()).isFalse();
    }

    @Test
    public void testNeedToCollectFeaturesEventLongAgoCollectingDisabledStartupValid() {
        when(mEventSaver.wasLastFeaturesEventLongAgo()).thenReturn(true);
        when(mReportRequestConfig.isFeaturesCollectingEnabled()).thenReturn(false);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(true);
        assertThat(mComponentUnit.needToCollectFeatures()).isFalse();
    }

    @Test
    public void testNeedToCollectFeaturesEventLongAgoCollectingEnabledStartupNotValid() {
        when(mEventSaver.wasLastFeaturesEventLongAgo()).thenReturn(true);
        when(mReportRequestConfig.isFeaturesCollectingEnabled()).thenReturn(true);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(false);
        assertThat(mComponentUnit.needToCollectFeatures()).isFalse();
    }

    @Test
    public void testNeedToCollectFeaturesEventLongAgoCollectingEnabledStartupValid() {
        when(mEventSaver.wasLastFeaturesEventLongAgo()).thenReturn(true);
        when(mReportRequestConfig.isFeaturesCollectingEnabled()).thenReturn(true);
        when(mReportRequestConfig.isIdentifiersValid()).thenReturn(true);
        assertThat(mComponentUnit.needToCollectFeatures()).isTrue();
    }

    @Test
    public void testNeedToSendPreloadInfoTrackingDisabledClidsDoNotMatch() {
        final StartupState startupState =
                TestUtils.createDefaultStartupStateBuilder().withStartupDidNotOverrideClids(false).build();
        when(mPreloadInfoStorage.retrieveData())
                .thenReturn(new PreloadInfoState("11", new JSONObject(), true, false, DistributionSource.APP));
        when(mConfigHolder.getStartupState()).thenReturn(startupState);
        assertThat(mComponentUnit.shouldSend()).isTrue();
    }

    @Test
    public void testNeedToSendPreloadInfoTrackingDisabledClidsMatch() {
        final StartupState startupState =
                TestUtils.createDefaultStartupStateBuilder().withStartupDidNotOverrideClids(true).build();
        when(mPreloadInfoStorage.retrieveData())
                .thenReturn(new PreloadInfoState("11", new JSONObject(), true, false, DistributionSource.APP));
        when(mConfigHolder.getStartupState()).thenReturn(startupState);
        assertThat(mComponentUnit.shouldSend()).isTrue();
    }

    @Test
    public void testNeedToSendPreloadInfoTrackingEnabledClidsDoNotMatch() {
        final StartupState startupState =
                TestUtils.createDefaultStartupStateBuilder().withStartupDidNotOverrideClids(false).build();
        when(mPreloadInfoStorage.retrieveData())
                .thenReturn(new PreloadInfoState("11", new JSONObject(), true, true, DistributionSource.APP));
        when(mConfigHolder.getStartupState()).thenReturn(startupState);
        assertThat(mComponentUnit.shouldSend()).isTrue();
    }

    @Test
    public void testNeedToSendPreloadInfoTrackingEnabledClidsMatch() {
        final StartupState startupState =
                TestUtils.createDefaultStartupStateBuilder().withStartupDidNotOverrideClids(true).build();
        when(mPreloadInfoStorage.retrieveData())
                .thenReturn(new PreloadInfoState("11", new JSONObject(), true, true, DistributionSource.APP));
        when(mConfigHolder.getStartupState()).thenReturn(startupState);
        assertThat(mComponentUnit.shouldSend()).isFalse();
    }

    @Test
    public void testGetComponentPreferences() {
        assertThat(mComponentUnit.getComponentPreferences()).isEqualTo(mComponentPreferences);
    }

    @Test
    public void testSetProfileId() {
        final String profileId = "777888999";
        when(mComponentPreferences.putProfileID(anyString())).thenReturn(mComponentPreferences);
        mComponentUnit.setProfileID(profileId);
        verify(mComponentPreferences).putProfileID(profileId);
        verify(mComponentPreferences).commit();
    }

    @Test
    public void testGetEventFirstOccurrenceService() {
        assertThat(mComponentUnit.getEventFirstOccurrenceService()).isEqualTo(mEventFirstOccurrenceService);
    }

    @Test
    public void testGetProfileId() {
        final String profileId = "1111122222";
        when(mComponentPreferences.getProfileID()).thenReturn(profileId);
        assertThat(mComponentUnit.getProfileID()).isEqualTo(profileId);
    }

    @Test
    public void testGetCertificateFingerprintProvider() {
        assertThat(mComponentUnit.getCertificatesFingerprintsProvider()).isEqualTo(mCertificatesFingerprintsProvider);
    }

    @Test
    public void getVitalComponentDataProvider() {
        assertThat(mComponentUnit.getVitalComponentDataProvider()).isSameAs(vitalComponentDataProvider);
    }

    @Test
    public void getSessionExtrasHolder() {
        assertThat(mComponentUnit.getSessionExtrasHolder()).isEqualTo(sessionExtrasHolder);
    }

    @Test
    public abstract void testGetReporterType();

    private CommonArguments.ReporterArguments createReporterArgumentsWithLogsEnabled(@Nullable Boolean enabled) {
        return new CommonArguments.ReporterArguments(null, null, null, null, null, null, null, enabled, null, null, null, null, null);
    }
}
