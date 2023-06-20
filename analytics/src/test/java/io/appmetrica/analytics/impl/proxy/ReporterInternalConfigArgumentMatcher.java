package io.appmetrica.analytics.impl.proxy;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.ReporterConfig;
import org.mockito.ArgumentMatcher;

class ReporterInternalConfigArgumentMatcher implements ArgumentMatcher<ReporterConfig> {

    @NonNull
    private final String mApiKey;

    public ReporterInternalConfigArgumentMatcher(@NonNull String apiKey) {
        mApiKey = apiKey;
    }

    @Override
    public boolean matches(ReporterConfig argument) {
        return mApiKey.equals(argument.apiKey);
    }
}
