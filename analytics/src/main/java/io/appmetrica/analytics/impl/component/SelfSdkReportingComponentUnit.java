package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.services.PackageManagerUtils;
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.executor.ComponentStartupExecutorFactory;
import io.appmetrica.analytics.internal.CounterConfigurationReporterType;

public class SelfSdkReportingComponentUnit extends ComponentUnit {

    public SelfSdkReportingComponentUnit(@NonNull Context context,
                                         @NonNull StartupState startupState,
                                         @NonNull ComponentId componentId,
                                         @NonNull CommonArguments.ReporterArguments sdkConfig,
                                         @NonNull ReportRequestConfig.DataSendingStrategy dataSendingStrategy,
                                         @NonNull ComponentStartupExecutorFactory startupExecutorFactory,
                                         @NonNull ComponentEventTriggerProviderCreator triggerProviderCreator) {
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
                triggerProviderCreator
            ),
            sdkConfig
        );
    }

    @VisibleForTesting
    SelfSdkReportingComponentUnit(@NonNull Context context,
                                  @NonNull ComponentId componentId,
                                  @NonNull AppEnvironmentProvider appEnvironmentProvider,
                                  @NonNull TimePassedChecker timePassedChecker,
                                  @NonNull ComponentUnitFieldsFactory fieldsFactory,
                                  @NonNull CommonArguments.ReporterArguments sdkConfig) {
        super(context, componentId, appEnvironmentProvider, timePassedChecker, fieldsFactory, sdkConfig);
    }

    @NonNull
    @Override
    public CounterConfigurationReporterType getReporterType() {
        return CounterConfigurationReporterType.SELF_SDK;
    }
}
