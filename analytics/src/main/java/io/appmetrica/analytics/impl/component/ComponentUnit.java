package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils;
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.CertificatesFingerprintsProvider;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.PreloadInfoStorage;
import io.appmetrica.analytics.impl.ReportingTaskProcessor;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.processor.EventProcessingStrategyFactory;
import io.appmetrica.analytics.impl.component.processor.ReportingReportProcessor;
import io.appmetrica.analytics.impl.component.remarketing.EventFirstOccurrenceService;
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine;
import io.appmetrica.analytics.impl.component.session.SessionState;
import io.appmetrica.analytics.impl.component.sessionextras.SessionExtrasHolder;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.events.EventTrigger;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupError;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.executor.ComponentStartupExecutorFactory;
import io.appmetrica.analytics.impl.utils.BooleanUtils;
import io.appmetrica.analytics.impl.utils.PublicLogConstructor;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;

/**
 * Represents bound component (application, library) with API key.
 */
public class ComponentUnit implements IReportableComponent, IComponent,
    ReportRequestConfig.PreloadInfoSendingStrategy {

    private static final String TAG = "[ComponentUnit]";

    @NonNull
    private final Context mContext;
    @NonNull
    private final ComponentId mComponentId;

    @NonNull
    private final PreferencesComponentDbStorage mComponentPreferences;
    @NonNull
    private final PreferencesServiceDbStorage mServicePreferences;

    @NonNull
    private final ReportingTaskProcessor mTaskProcessor;
    @NonNull
    private final DatabaseHelper mReportsDbHelper;
    @NonNull
    private final ReportingReportProcessor mReportProcessor;
    @NonNull
    private final EventProcessingStrategyFactory mEventProcessingStrategyFactory;

    @NonNull
    private final AppEnvironment mAppEnvironment;
    @NonNull
    private final AppEnvironmentProvider mAppEnvironmentProvider;

    @NonNull
    private final SessionManagerStateMachine mSessionManager;

    @NonNull
    private final ReportComponentConfigurationHolder mConfigHolder;
    @NonNull
    private final EventFirstOccurrenceService mEventFirstOccurrenceService;
    @NonNull
    private final PublicLogger mPublicLogger;
    @NonNull
    private final EventSaver mEventSaver;
    @NonNull
    private final ComponentMigrationHelper.Creator mComponentMigrationHelperCreator;
    @NonNull
    private final EventTrigger conditionalEventTrigger;
    @NonNull
    private final CertificatesFingerprintsProvider mCertificatesFingerprintsProvider;
    @NonNull
    private final TimePassedChecker mTimePassedChecker;
    @NonNull
    private final PreloadInfoStorage mPreloadInfoStorage;
    @NonNull
    private final VitalComponentDataProvider vitalComponentDataProvider;
    @NonNull
    private final SessionExtrasHolder sessionExtrasHolder;

    @NonNull
    public SessionManagerStateMachine getSessionManager() {
        return mSessionManager;
    }

    @NonNull
    public EventSaver getEventSaver() {
        return mEventSaver;
    }

    public ComponentUnit(@NonNull Context context,
                         @NonNull StartupState startupState,
                         @NonNull ComponentId componentId,
                         @NonNull CommonArguments.ReporterArguments sdkConfig,
                         @NonNull ReportRequestConfig.DataSendingStrategy dataSendingStrategy,
                         @NonNull ComponentStartupExecutorFactory startupExecutorFactory) {
        this(
            context,
            componentId,
            new AppEnvironmentProvider(),
            new TimePassedChecker(),
            new ComponentUnitFieldsFactory(
                context,
                componentId,
                sdkConfig,
                startupExecutorFactory,
                startupState,
                dataSendingStrategy,
                GlobalServiceLocator.getInstance().getServiceExecutorProvider()
                    .getNetworkTaskProcessorExecutor(),
                PackageManagerUtils.getAppVersionCodeInt(context),
                GlobalServiceLocator.getInstance().getLifecycleDependentComponentManager(),
                new ComponentEventTriggerProviderCreator()
            ),
            sdkConfig
        );
    }

    @SuppressWarnings("checkstyle:methodLength")
    @VisibleForTesting
    ComponentUnit(@NonNull Context context,
                  @NonNull ComponentId componentId,
                  @NonNull AppEnvironmentProvider appEnvironmentProvider,
                  @NonNull TimePassedChecker timePassedChecker,
                  @NonNull ComponentUnitFieldsFactory fieldsFactory,
                  @NonNull CommonArguments.ReporterArguments sdkConfig) {
        DebugLogger.INSTANCE.info(
            TAG,
            "Start to create a new component with Id/APIkey: \"%s\"/%s",
            componentId,
            componentId.getAnonymizedApiKey()
        );

        mContext = context.getApplicationContext();
        mComponentId = componentId;
        mAppEnvironmentProvider = appEnvironmentProvider;
        mTimePassedChecker = timePassedChecker;
        vitalComponentDataProvider = fieldsFactory.getVitalComponentDataProvider();
        mPreloadInfoStorage = GlobalServiceLocator.getInstance().getPreloadInfoStorage();
        mConfigHolder = fieldsFactory.createConfigHolder(this);
        mPublicLogger = fieldsFactory.getLoggerProvider().getPublicLogger();
        mComponentPreferences = fieldsFactory.getPreferencesProvider().createPreferencesComponentDbStorage();
        mServicePreferences = GlobalServiceLocator.getInstance().getServicePreferences();
        mAppEnvironment = appEnvironmentProvider.getOrCreate(mComponentId, mPublicLogger, mComponentPreferences);
        mEventFirstOccurrenceService = fieldsFactory.createEventFirstOccurrenceService();
        mReportsDbHelper = fieldsFactory.createDatabaseHelper(this);
        mTaskProcessor = fieldsFactory.createTaskProcessor(this);

        DebugLogger.INSTANCE.info(
            TAG,
            "create holder for a new component with Id/APIkey: \"%s\"/%s",
            componentId,
            componentId.getAnonymizedApiKey()
        );
        mComponentMigrationHelperCreator = fieldsFactory.createMigrationHelperCreator(this);

        migratePreferencesIfNeeded();

        mSessionManager = fieldsFactory.createSessionManager(
            this,
            vitalComponentDataProvider,
            new SessionManagerStateMachine.EventSaver() {
                public void saveEvent(@NonNull CounterReport reportData, @NonNull SessionState sessionState) {
                    mEventSaver.saveReport(reportData, sessionState);
                }
            }
        );

        mPublicLogger.info(
            "Read app environment for component %s. Value: %s",
            mComponentId.toString(),
            mAppEnvironment.getLastRevision().value
        );

        sessionExtrasHolder = fieldsFactory.createSessionExtraHolder();
        mEventSaver = fieldsFactory.createReportSaver(
            mComponentPreferences,
            vitalComponentDataProvider,
            mSessionManager,
            mReportsDbHelper,
            mAppEnvironment,
            sessionExtrasHolder,
            mTaskProcessor
        );

        mEventProcessingStrategyFactory = fieldsFactory.createEventProcessingStrategyFactory(this);
        mReportProcessor = fieldsFactory.createReportProcessor(this, mEventProcessingStrategyFactory);
        mCertificatesFingerprintsProvider = fieldsFactory.createCertificateFingerprintProvider(mComponentPreferences);
        conditionalEventTrigger = fieldsFactory.createEventTrigger(
            mTaskProcessor,
            mReportsDbHelper,
            mConfigHolder,
            sdkConfig,
            componentId,
            mComponentPreferences
        );

        mReportsDbHelper.onComponentCreated();
        DebugLogger.INSTANCE.info(
            TAG,
            "Create a new component with Id/APIkey: \"%s\"/%s",
            componentId,
            componentId.getAnonymizedApiKey()
        );
    }

    @NonNull
    public StartupState getStartupState() {
        return mConfigHolder.getStartupState();
    }

    @NonNull
    protected EventProcessingStrategyFactory getEventProcessingStrategyFactory() {
        return mEventProcessingStrategyFactory;
    }

    @Override
    public void handleReport(@NonNull CounterReport reportData) {
        DebugLogger.INSTANCE.info(
            TAG,
            "A new report for component \"%s\", data: %s",
            mComponentId,
            reportData
        );
        logEvent(reportData, "Event received on service");

        // Just to be sure. Don't report if API key isn't defined for some reasons
        if (!Utils.isApiKeyDefined(mComponentId.getApiKey())) {
            DebugLogger.INSTANCE.warning(TAG, "Attempt to send report when API key isn't defined correctly");
            return;
        }

        mReportProcessor.process(reportData);
    }

    @Override
    public synchronized void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig) {
        mConfigHolder.updateArguments(sdkConfig);
        updateLoggerEnabledState(sdkConfig);
    }

    public synchronized void flushEvents() {
        DebugLogger.INSTANCE.info(TAG, "Flushing has started for component with id \"%s\" ...", mComponentId);
        conditionalEventTrigger.forceTrigger();
    }

    @NonNull
    public ReportRequestConfig getFreshReportRequestConfig() {
        return mConfigHolder.get();
    }

    @NonNull
    public DatabaseHelper getDbHelper() {
        return mReportsDbHelper;
    }

    @Override
    @NonNull
    public ComponentId getComponentId() {
        return mComponentId;
    }

    @Override
    public synchronized void onStartupChanged(@NonNull StartupState newState) {
        mConfigHolder.updateStartupState(newState);
        DebugLogger.INSTANCE.info(TAG, "%s startup changed. new StartupState: %s", mComponentId, newState);
        conditionalEventTrigger.trigger();
    }

    @Override
    public void onStartupError(@NonNull StartupError error, @Nullable StartupState existingState) {

    }

    @Override
    @NonNull
    public Context getContext() {
        return mContext;
    }

    @NonNull
    public PublicLogger getPublicLogger() {
        return mPublicLogger;
    }

    public void markFeaturesChecked() {
        mEventSaver.saveFeaturesCheckVersion();
    }

    public void addAppEnvironmentValue(CounterReport report) {
        mAppEnvironment.add(report.getAppEnvironment());
        AppEnvironment.EnvironmentRevision revision = mAppEnvironment.getLastRevision();
        if (mAppEnvironmentProvider.commitIfNeeded(revision, mComponentPreferences)) {
            mPublicLogger.info("Save new app environment for %s. Value: %s", getComponentId(), revision.value);
        }
    }

    public void clearAppEnvironment() {
        mAppEnvironment.reset();
        mAppEnvironmentProvider.commit(mAppEnvironment.getLastRevision(), mComponentPreferences);
    }

    public void resetConfigHolder() {
        mConfigHolder.reset();
    }

    public boolean needToCheckPermissions() {
        final ReportRequestConfig reportRequestConfig = getFreshReportRequestConfig();
        return reportRequestConfig.isPermissionsCollectingEnabled() &&
            reportRequestConfig.isIdentifiersValid() &&
            mTimePassedChecker.didTimePassSeconds(
                mEventSaver.getPermissionsCheckTime(),
                reportRequestConfig.getPermissionsCollectingIntervalSeconds(),
                "need to check permissions"
            );
    }

    public boolean shouldForceSendPermissions() {
        final ReportRequestConfig reportRequestConfig = getFreshReportRequestConfig();
        return reportRequestConfig.isPermissionsCollectingEnabled() &&
            mTimePassedChecker.didTimePassSeconds(
                mEventSaver.getPermissionsCheckTime(),
                reportRequestConfig.getPermissionsForceSendIntervalSeconds(),
                "should force send permissions"
            );
    }

    public boolean needToCollectFeatures() {
        return mEventSaver.wasLastFeaturesEventLongAgo()
            && getFreshReportRequestConfig().isFeaturesCollectingEnabled()
            && getFreshReportRequestConfig().isIdentifiersValid();
    }

    @Override
    public boolean shouldSend() {
        boolean preloadInfoDetectionEnabled = mPreloadInfoStorage.retrieveData().autoTrackingEnabled;
        boolean startupDidNotOverrideClids = mConfigHolder.getStartupState().getStartupDidNotOverrideClids();
        return (preloadInfoDetectionEnabled && startupDidNotOverrideClids) == false;
    }

    private void migratePreferencesIfNeeded() {
        int apiLevel = AppMetrica.getLibraryApiLevel();
        Integer lastApiLevel = vitalComponentDataProvider.getLastMigrationApiLevel();
        if (lastApiLevel < apiLevel) {
            mComponentMigrationHelperCreator.create(this).migrate(lastApiLevel);
            vitalComponentDataProvider.setLastMigrationApiLevel(apiLevel);
        }
    }

    private void updateLoggerEnabledState(@NonNull CommonArguments.ReporterArguments config) {
        if (BooleanUtils.isTrue(config.logEnabled)) {
            mPublicLogger.setEnabled(true);
        } else if (BooleanUtils.isFalse(config.logEnabled)) {
            mPublicLogger.setEnabled(false);
        }
    }

    @NonNull
    public PreferencesComponentDbStorage getComponentPreferences() {
        return mComponentPreferences;
    }

    @NonNull
    public VitalComponentDataProvider getVitalComponentDataProvider() {
        return vitalComponentDataProvider;
    }

    @NonNull
    public PreferencesServiceDbStorage getServicePreferences() {
        return mServicePreferences;
    }

    public void setProfileID(@Nullable String profileID) {
        mComponentPreferences.putProfileID(profileID).commit();
    }

    @NonNull
    public EventFirstOccurrenceService getEventFirstOccurrenceService() {
        return mEventFirstOccurrenceService;
    }

    @NonNull
    public CertificatesFingerprintsProvider getCertificatesFingerprintsProvider() {
        return mCertificatesFingerprintsProvider;
    }

    @Nullable
    public String getProfileID() {
        return mComponentPreferences.getProfileID();
    }

    @NonNull
    public EventTrigger getEventTrigger() {
        return conditionalEventTrigger;
    }

    @NonNull
    @Override
    public CounterConfigurationReporterType getReporterType() {
        return CounterConfigurationReporterType.MANUAL;
    }

    public void subscribeForReferrer() {
        // override this method to actually subscribe
        // this should be done for main api keys only
    }

    @NonNull
    public SessionExtrasHolder getSessionExtrasHolder() {
        return sessionExtrasHolder;
    }

    private void logEvent(final CounterReport reportData, String msg) {
        String log = PublicLogConstructor.constructCounterReportLog(reportData, msg);
        if (log != null) {
            mPublicLogger.info(log);
        }
    }
}
