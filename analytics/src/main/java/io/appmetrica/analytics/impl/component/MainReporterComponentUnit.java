package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils;
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.processor.EventProcessingStrategyFactory;
import io.appmetrica.analytics.impl.component.processor.factory.RegularMainReporterFactory;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.executor.ComponentStartupExecutorFactory;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_REGULAR;

public class MainReporterComponentUnit extends ComponentUnit {

    private static final String TAG = "[MainMasterComponentUnit]";

    @NonNull
    private final DataSendingRestrictionControllerImpl dateSendingRestrictionController;

    public MainReporterComponentUnit(@NonNull Context context,
                                     @NonNull StartupState startupState,
                                     @NonNull ComponentId componentId,
                                     @NonNull CommonArguments.ReporterArguments sdkConfig,
                                     @NonNull DataSendingRestrictionControllerImpl controller,
                                     @NonNull ComponentStartupExecutorFactory executorFactory) {
        this(
            context,
            componentId,
            startupState,
            sdkConfig,
            new AppEnvironmentProvider(),
            new TimePassedChecker(),
            new MainReporterComponentUnitFieldsFactory(
                context,
                componentId,
                sdkConfig,
                executorFactory,
                startupState,
                new MainReporterArgumentsFactory(controller),
                GlobalServiceLocator.getInstance().getServiceExecutorProvider()
                    .getNetworkTaskProcessorExecutor(),
                PackageManagerUtils.getAppVersionCodeInt(context),
                GlobalServiceLocator.getInstance().getServiceExecutorProvider(),
                GlobalServiceLocator.getInstance().getLifecycleDependentComponentManager(),
                new MainComponentEventTriggerProviderCreator()
            ),
            controller
        );
    }

    @VisibleForTesting
    MainReporterComponentUnit(@NonNull Context context,
                              @NonNull ComponentId componentId,
                              @NonNull StartupState startupState,
                              @NonNull CommonArguments.ReporterArguments sdkConfig,
                              @NonNull AppEnvironmentProvider appEnvironmentProvider,
                              @NonNull TimePassedChecker timePassedChecker,
                              @NonNull MainReporterComponentUnitFieldsFactory fieldsFactory,
                              @NonNull DataSendingRestrictionControllerImpl controller) {
        super(
            context,
            componentId,
            appEnvironmentProvider,
            timePassedChecker,
            fieldsFactory,
            sdkConfig
        );
        EventProcessingStrategyFactory factory = getEventProcessingStrategyFactory();
        factory.mutateHandlers(EVENT_TYPE_REGULAR, new RegularMainReporterFactory(factory.getHandlersProvider()));
        dateSendingRestrictionController = controller;
        GlobalServiceLocator.getInstance().getServiceModuleReporterComponentLifecycle()
            .onMainReporterCreated(
                new ServiceModuleReporterComponentContextImpl(this, sdkConfig)
            );
    }

    @Override
    public synchronized void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig) {
        super.updateSdkConfig(sdkConfig);
        DebugLogger.INSTANCE.info(TAG, "updateSdkConfig: %s", sdkConfig);
        dateSendingRestrictionController.setEnabledFromMainReporter(sdkConfig.dataSendingEnabled);
    }

    @Override
    @NonNull
    public CounterConfigurationReporterType getReporterType() {
        return CounterConfigurationReporterType.MAIN;
    }
}
