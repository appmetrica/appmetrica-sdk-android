package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetricaConfig;

public class MainOrCrashReporterProvider extends UnhandledSituationReporterProvider {

    @NonNull
    private final AppMetricaConfig mConfig;

    public MainOrCrashReporterProvider(@NonNull IReporterFactoryProvider reporterFactoryProvider,
                                       @NonNull AppMetricaConfig config) {
        super(reporterFactoryProvider, config.apiKey);
        mConfig = config;
    }

    @Override
    @NonNull
    public IUnhandledSituationReporter getReporter() {
        return mReporterFactoryProvider.getReporterFactory().getMainOrCrashReporter(mConfig);
    }
}
