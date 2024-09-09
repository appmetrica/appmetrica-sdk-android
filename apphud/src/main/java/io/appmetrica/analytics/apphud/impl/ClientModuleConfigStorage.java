package io.appmetrica.analytics.apphud.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.config.client.ClientApphudConfig;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.ClientStorageProvider;
import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences;

public class ClientModuleConfigStorage {

    private static final String TAG = "[ClientModuleConfigStorage]";

    @NonNull
    private final ModulePreferences modulePreferences;

    public ClientModuleConfigStorage(
        @NonNull ClientStorageProvider clientStorageProvider
    ) {
        this.modulePreferences = clientStorageProvider.modulePreferences(Constants.MODULE_ID);
    }

    @NonNull
    public synchronized ClientApphudConfig load() {
        String apiKey = modulePreferences.getString(Constants.ClientConfig.API_KEY_KEY, null);
        String deviceId = modulePreferences.getString(Constants.ClientConfig.DEVICE_ID_KEY, null);
        String uuid = modulePreferences.getString(Constants.ClientConfig.UUID_KEY, null);
        ClientApphudConfig config = new ClientApphudConfig(apiKey, deviceId, uuid);
        DebugLogger.INSTANCE.info(TAG, "Loaded config " + config);
        return config;
    }

    public synchronized void save(@NonNull ClientApphudConfig config) {
        DebugLogger.INSTANCE.info(TAG, "Save config " + config);
        modulePreferences.putString(Constants.ClientConfig.API_KEY_KEY, config.getApiKey());
        modulePreferences.putString(Constants.ClientConfig.DEVICE_ID_KEY, config.getDeviceId());
        modulePreferences.putString(Constants.ClientConfig.UUID_KEY, config.getUuid());
    }
}
