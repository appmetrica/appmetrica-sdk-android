package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils;
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.StatisticsRestrictionControllerImpl;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.executor.ComponentStartupExecutorFactory;

public class ReporterComponentUnit extends ComponentUnit {

    private final String mApiKey;
    private final StatisticsRestrictionControllerImpl mStatisticsRestrictionController;

    public ReporterComponentUnit(@NonNull Context context,
                                 @NonNull ComponentId componentId,
                                 @NonNull CommonArguments.ReporterArguments sdkConfig,
                                 @NonNull StatisticsRestrictionControllerImpl controller,
                                 @NonNull StartupState startupState,
                                 @NonNull ComponentStartupExecutorFactory factory) {
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
                        GlobalServiceLocator.getInstance().getLifecycleDependentComponentManager()
                ),
                controller
        );
    }

    @VisibleForTesting
    ReporterComponentUnit(@NonNull Context context,
                          @NonNull ComponentId componentId,
                          @NonNull AppEnvironmentProvider appEnvironmentProvider,
                          @NonNull TimePassedChecker timePassedChecker,
                          @NonNull ComponentUnitFieldsFactory fieldsFactory,
                          @NonNull StatisticsRestrictionControllerImpl controller) {
        super(
                context,
                componentId,
                appEnvironmentProvider,
                timePassedChecker,
                fieldsFactory
        );
        mApiKey = componentId.getApiKey();
        mStatisticsRestrictionController = controller;
    }

    @Override
    public synchronized void updateSdkConfig(@NonNull CommonArguments.ReporterArguments sdkConfig) {
        super.updateSdkConfig(sdkConfig);
        mStatisticsRestrictionController
                .setEnabledFromSharedReporter(
                        mApiKey,
                        sdkConfig.statisticsSending
                );
    }
}
