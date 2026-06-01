package io.appmetrica.analytics.apphud.impl.config.service.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ServiceSideApphudConfig {

    private final boolean enabled;
    @Nullable
    private final String apiKey;

    public ServiceSideApphudConfig(
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
        return "ServiceSideApphudConfig{" +
            "enabled=" + enabled +
            ", apiKey='" + apiKey + '\'' +
            '}';
    }
}
