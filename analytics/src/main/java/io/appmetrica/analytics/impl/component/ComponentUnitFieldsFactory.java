package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.CertificatesFingerprintsProvider;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.LifecycleDependentComponentManager;
import io.appmetrica.analytics.impl.ReportingTaskProcessor;
import io.appmetrica.analytics.impl.component.processor.EventProcessingStrategyFactory;
import io.appmetrica.analytics.impl.component.processor.ReportingReportProcessor;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import io.appmetrica.analytics.impl.component.remarketing.EventFirstOccurrenceService;
import io.appmetrica.analytics.impl.component.session.SessionIDProvider;
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine;
import io.appmetrica.analytics.impl.component.sessionextras.SessionExtrasHolder;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.impl.events.ContainsUrgentEventsCondition;
import io.appmetrica.analytics.impl.events.EventCondition;
import io.appmetrica.analytics.impl.events.EventTrigger;
import io.appmetrica.analytics.impl.events.EventsFlusher;
import io.appmetrica.analytics.impl.events.MaxReportsCountReachedCondition;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.executor.ComponentStartupExecutorFactory;
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypterProvider;
import java.util.List;

class ComponentUnitFieldsFactory {

    static class LoggerProvider {

        @Nullable
        private final String mApiKey;

        LoggerProvider(@Nullable String apiKey) {
            mApiKey = apiKey;
        }

        PublicLogger getPublicLogger() {
            return LoggerStorage.getOrCreatePublicLogger(mApiKey);
        }
    }

    static class PreferencesProvider {

        @NonNull
        private final ComponentId mComponentId;
        @NonNull
        private final DatabaseStorageFactory mDatabaseStorageFactory;

        PreferencesProvider(@NonNull Context context,
                            @NonNull ComponentId componentId) {
            this(componentId, DatabaseStorageFactory.getInstance(context));
        }

        @VisibleForTesting
        PreferencesProvider(@NonNull ComponentId componentId,
                            @NonNull DatabaseStorageFactory databaseStorageFactory) {
            mComponentId = componentId;
            mDatabaseStorageFactory = databaseStorageFactory;
        }

        @NonNull
        PreferencesComponentDbStorage createPreferencesComponentDbStorage() {
            return new PreferencesComponentDbStorage(mDatabaseStorageFactory.getPreferencesDbHelper(mComponentId));
        }
    }

    @NonNull
    private final LoggerProvider mLoggerProvider;
    @NonNull
    private final PreferencesProvider mPreferencesProvider;
    @NonNull
    protected final Context mContext;
    @NonNull
    private final ComponentId mComponentId;
    @NonNull
    private final CommonArguments.ReporterArguments mSdkConfig;
    @NonNull
    private final ComponentStartupExecutorFactory mStartupExecutorFactory;
    @NonNull
    protected final StartupState mStartupState;
    @NonNull
    private final ReportRequestConfig.DataSendingStrategy mDataSendingStrategy;
    @NonNull
    private final EventEncrypterProvider mEventEncrypterProvider;
    @NonNull
    private final ICommonExecutor mTaskExecutor;
    @NonNull
    private final LifecycleDependentComponentManager lifecycleDependentComponentManager;
    private final int mCurrentAppVersion;

    ComponentUnitFieldsFactory(@NonNull Context context,
                               @NonNull ComponentId componentId,
                               @NonNull CommonArguments.ReporterArguments sdkConfig,
                               @NonNull ComponentStartupExecutorFactory startupExecutorFactory,
                               @NonNull StartupState startupState,
                               @NonNull ReportRequestConfig.DataSendingStrategy statSendingStrategy,
                               @NonNull ICommonExecutor taskExecutor,
                               final int currentAppVersion,
                               @NonNull LifecycleDependentComponentManager lifecycleDependentComponentManager) {
        this(
                context,
                componentId,
                sdkConfig,
                startupExecutorFactory,
                startupState,
                statSendingStrategy,
                taskExecutor,
                new EventEncrypterProvider(),
                currentAppVersion,
                new LoggerProvider(sdkConfig.apiKey),
                new PreferencesProvider(context, componentId),
                lifecycleDependentComponentManager
        );
    }

    @VisibleForTesting
    ComponentUnitFieldsFactory(@NonNull Context context,
                               @NonNull ComponentId componentId,
                               @NonNull CommonArguments.ReporterArguments sdkConfig,
                               @NonNull ComponentStartupExecutorFactory startupExecutorFactory,
                               @NonNull StartupState startupState,
                               @NonNull ReportRequestConfig.DataSendingStrategy statSendingStrategy,
                               @NonNull ICommonExecutor taskExecutor,
                               @NonNull EventEncrypterProvider eventEncrypterProvider,
                               final int currentAppVersion,
                               @NonNull LoggerProvider loggerProvider,
                               @NonNull PreferencesProvider preferencesProvider,
                               @NonNull LifecycleDependentComponentManager lifecycleDependentComponentManager) {
        mContext = context;
        mComponentId = componentId;
        mSdkConfig = sdkConfig;
        mStartupExecutorFactory = startupExecutorFactory;
        mStartupState = startupState;
        mDataSendingStrategy = statSendingStrategy;
        mTaskExecutor = taskExecutor;
        mEventEncrypterProvider = eventEncrypterProvider;
        mCurrentAppVersion = currentAppVersion;
        mLoggerProvider = loggerProvider;
        mPreferencesProvider = preferencesProvider;
        this.lifecycleDependentComponentManager = lifecycleDependentComponentManager;
    }

    @NonNull
    LoggerProvider getLoggerProvider() {
        return mLoggerProvider;
    }

    @NonNull
    PreferencesProvider getPreferencesProvider() {
        return mPreferencesProvider;
    }

    @NonNull
    EventFirstOccurrenceService createEventFirstOccurrenceService() {
        return new EventFirstOccurrenceService(mContext, mComponentId, mCurrentAppVersion);
    }

    @NonNull
    DatabaseHelper createDatabaseHelper(@NonNull ComponentUnit componentUnit) {
        return new DatabaseHelper(componentUnit,
                DatabaseStorageFactory.getInstance(mContext).getStorageForComponent(mComponentId));
    }

    @NonNull
    ReportingTaskProcessor<ComponentUnit> createTaskProcessor(@NonNull ComponentUnit component) {
        ReportingTaskProcessor<ComponentUnit> processor =  new ReportingTaskProcessor<ComponentUnit>(
                component,
                mStartupExecutorFactory.<ComponentUnit>create(),
                mTaskExecutor
        );
        lifecycleDependentComponentManager.addLifecycleObserver(processor);
        return processor;
    }

    @NonNull
    ReportComponentConfigurationHolder createConfigHolder(@NonNull ComponentUnit componentUnit) {
        return new ReportComponentConfigurationHolder(
                new ReportRequestConfig.Loader(componentUnit, mDataSendingStrategy),
                mStartupState,
                new ReportRequestConfig.Arguments(mSdkConfig)
        );
    }

    @NonNull
    SessionManagerStateMachine createSessionManager(@NonNull ComponentUnit componentUnit,
                                                    @NonNull VitalComponentDataProvider vitalComponentDataProvider,
                                                    @NonNull SessionManagerStateMachine.EventSaver eventSaver) {
        return new SessionManagerStateMachine(
                componentUnit,
                new SessionIDProvider(vitalComponentDataProvider),
                eventSaver
        );
    }

    @NonNull
    EventSaver createReportSaver(@NonNull PreferencesComponentDbStorage componentPreferences,
                                 @NonNull VitalComponentDataProvider vitalComponentDataProvider,
                                 @NonNull SessionManagerStateMachine sessionManager,
                                 @NonNull DatabaseHelper databaseHelper,
                                 @NonNull AppEnvironment appEnvironment,
                                 @NonNull SessionExtrasHolder sessionExtrasHolder,
                                 @NonNull final ReportingTaskProcessor taskProcessor) {
        return new EventSaver(
                componentPreferences,
                vitalComponentDataProvider,
                sessionManager,
                databaseHelper,
                appEnvironment,
                mEventEncrypterProvider,
                sessionExtrasHolder,
                mCurrentAppVersion,
                new EventSaver.ReportSavedListener() {
                    @Override
                    public void onReportSaved() {
                        taskProcessor.restartFlushTask();
                    }
                }
        );
    }

    @NonNull
    EventProcessingStrategyFactory createEventProcessingStrategyFactory(@NonNull ComponentUnit component) {
        return new EventProcessingStrategyFactory(component);
    }

    @NonNull
    ReportingReportProcessor<ReportComponentHandler, ComponentUnit>
    createReportProcessor(@NonNull ComponentUnit component,
                          @NonNull EventProcessingStrategyFactory eventProcessingStrategyFactory) {
        return new ReportingReportProcessor<ReportComponentHandler, ComponentUnit>(
                eventProcessingStrategyFactory,
                component
        );
    }

    @NonNull
    ComponentMigrationHelper.Creator createMigrationHelperCreator(@NonNull ComponentUnit componentUnit) {
        return new ComponentMigrationHelper.Creator();
    }

    @NonNull
    ContainsUrgentEventsCondition createUrgentEventsCondition(@NonNull DatabaseHelper databaseHelper) {
        return new ContainsUrgentEventsCondition(databaseHelper);
    }

    @NonNull
    MaxReportsCountReachedCondition createMaxReportsCondition(
            @NonNull DatabaseHelper databaseHelper,
            @NonNull ReportComponentConfigurationHolder configHolder) {
        return new MaxReportsCountReachedCondition(databaseHelper, configHolder);
    }

    @NonNull
    EventTrigger createEventTrigger(@NonNull List<EventCondition> conditions,
                                    @NonNull EventsFlusher eventsFlusher) {
        return new EventTrigger(conditions, eventsFlusher);
    }

    @NonNull
    CertificatesFingerprintsProvider createCertificateFingerprintProvider(
            @NonNull PreferencesComponentDbStorage preferences) {
        return new CertificatesFingerprintsProvider(mContext, preferences);
    }

    @NonNull
    VitalComponentDataProvider getVitalComponentDataProvider() {
        return GlobalServiceLocator.getInstance().getVitalDataProviderStorage()
                .getComponentDataProvider(mComponentId);
    }

    @NonNull
    SessionExtrasHolder createSessionExtraHolder() {
        return new SessionExtrasHolder(mContext, mComponentId);
    }
}
