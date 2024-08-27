package io.appmetrica.analytics.apphud.impl.config;

import androidx.annotation.NonNull;

public class ModuleConfig {

    @NonNull
    private final String apiKey;

    public ModuleConfig(@NonNull String apiKey) {
        this.apiKey = apiKey;
    }

    @NonNull
    public String getApiKey() {
        return apiKey;
    }
}
