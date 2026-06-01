package io.appmetrica.analytics.apphud.impl.config.client.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ClientSideApphudConfig {

    private final boolean enabled;
    @Nullable
    private final String apiKey;

    public ClientSideApphudConfig(
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
        return "ClientSideApphudConfig{" +
            "enabled=" + enabled +
            ", apiKey='" + apiKey + '\'' +
            '}';
    }
}
