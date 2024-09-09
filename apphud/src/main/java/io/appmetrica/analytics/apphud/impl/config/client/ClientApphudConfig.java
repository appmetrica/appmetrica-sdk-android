package io.appmetrica.analytics.apphud.impl.config.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ClientApphudConfig {

    @Nullable
    private final String apiKey;
    @Nullable
    private final String deviceId;
    @Nullable
    private final String uuid;

    public ClientApphudConfig(
        @Nullable String apiKey,
        @Nullable String deviceId,
        @Nullable String uuid
    ) {
        this.apiKey = apiKey;
        this.deviceId = deviceId;
        this.uuid = uuid;
    }

    @Nullable
    public String getApiKey() {
        return apiKey;
    }

    @Nullable
    public String getDeviceId() {
        return deviceId;
    }

    @Nullable
    public String getUuid() {
        return uuid;
    }

    @NonNull
    @Override
    public String toString() {
        return "ClientModuleConfig{" +
            "apiKey='" + apiKey + '\'' +
            ", deviceId='" + deviceId + '\'' +
            ", uuid='" + uuid + '\'' +
            '}';
    }
}
