package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils;
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.executor.ComponentStartupExecutorFactory;

public class ReporterComponentUnit extends ComponentUnit {

    private final String mApiKey;
    private final DataSendingRestrictionControllerImpl dataSendingRestrictionController;

    public ReporterComponentUnit(@NonNull Context context,
                                 @NonNull ComponentId componentId,
                                 @NonNull CommonArguments.ReporterArguments sdkConfig,
                                 @NonNull DataSendingRestrictionControllerImpl controller,
                                 @NonNull StartupState startupState,
                                 @NonNull ComponentStartupExecutorFactory factory,
                                 @NonNull EventTriggerProviderCreator eventTriggerProviderCreator) {
        this(
            context,
            componentId,
            new AppEnvironmentProvider(),
            new TimePassedChecker(),
            new ComponentUnitFieldsFactory(
                context,
                componentId,
                sdkConfig,
                factory,
                startupState,
                new ReporterArgumentsFactory(controller),
                GlobalServiceLocator.getInstance().getServiceExecutorProvider()
                    .getNetworkTaskProcessorExecutor(),
                PackageManagerUtils.getAppVersionCodeInt(context),
                GlobalServiceLocator.getInstance().getLifecycleDependentComponentManager(),
                eventTriggerProviderCreator
            ),
            controller,
            sdkConfig
        );
    }

    @VisibleForTesting
    ReporterComponentUnit(@NonNull Context context,
                          @NonNull ComponentId componentId,
                          @NonNull AppEnvironmentProvider appEnvironmentProvider,
                          @NonNull TimePassedChecker timePassedChecker,
                          @NonNull ComponentUnitFieldsFactory fieldsFactory,
                          @NonNull DataSendingRestrictionControllerImpl controller,
                          @NonNull CommonArguments.ReporterArguments sdkConfig) {
        super(
            context,
            componentId,
            appEnvironmentProvider,
            timePassedChecker,
            fieldsFactory,
            sdkConfig
        );
        mApiKey = componentId.getApiKey();
        dataSendingRestrictionController = controller;
    }

    @Override
    public synchronized void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig) {
        super.updateSdkConfig(sdkConfig);
        dataSendingRestrictionController
            .setEnabledFromSharedReporter(
                mApiKey,
                sdkConfig.dataSendingEnabled
            );
    }
}
