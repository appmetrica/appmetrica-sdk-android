package io.appmetrica.analytics.apphud.impl.config.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RemoteApphudConfig {

    private final boolean enabled;
    @Nullable
    private final String apiKey;

    public RemoteApphudConfig(
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
        return "RemoteModuleConfig{" +
            "enabled=" + enabled +
            ", apiKey='" + apiKey + '\'' +
            '}';
    }
}
