package io.appmetrica.analytics.apphud.impl.config.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ServiceApphudConfig {

    private final boolean enabled;
    @Nullable
    private final String apiKey;

    public ServiceApphudConfig(
        boolean enabled,
        @Nullable String apiKey
    ) {
        this.enabled = enabled;
        this.apiKey = apiKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Nullable
    public String getApiKey() {
        return apiKey;
    }

    @NonNull
    @Override
    public String toString() {
        return "ServiceModuleConfig{" +
            "enabled=" + enabled +
            ", apiKey='" + apiKey + '\'' +
            '}';
    }
}
