package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.ReporterConfig;

public class UnhandledSituationReporterProvider {

    @NonNull
    final String mApiKey;
    @NonNull
    final IReporterFactoryProvider mReporterFactoryProvider;

    public UnhandledSituationReporterProvider(@NonNull final IReporterFactoryProvider reporterFactoryProvider,
                                              @NonNull final String apiKey) {
        mReporterFactoryProvider = reporterFactoryProvider;
        mApiKey = apiKey;
    }

    @NonNull
    public IUnhandledSituationReporter getReporter() {
        return mReporterFactoryProvider.getReporterFactory()
                .getOrCreateReporter(ReporterConfig.newConfigBuilder(mApiKey).build());
    }
}
