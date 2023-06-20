package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.request.ComponentConfigurationHolder;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupState;

public class ReportComponentConfigurationHolder extends
        ComponentConfigurationHolder<ReportRequestConfig, ReportRequestConfig.Arguments, ReportRequestConfig.Loader> {

    ReportComponentConfigurationHolder(@NonNull ReportRequestConfig.Loader loader,
                                       @NonNull StartupState startupState,
                                       @NonNull ReportRequestConfig.Arguments configuration) {
        super(loader, startupState, configuration);
    }
}
