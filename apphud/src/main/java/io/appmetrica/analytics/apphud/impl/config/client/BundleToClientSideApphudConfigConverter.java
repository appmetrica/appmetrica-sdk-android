package io.appmetrica.analytics.apphud.impl.config.client;

import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.client.model.ClientSideApphudConfig;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.BundleToServiceConfigConverter;

public class BundleToClientSideApphudConfigConverter implements BundleToServiceConfigConverter<ClientSideApphudConfig> {

    private static final String TAG = "[BundleToClientSideApphudConfigConverter]";

    @NonNull
    @Override
    public ClientSideApphudConfig fromBundle(@NonNull Bundle bundle) {
        boolean enabled = bundle.getBoolean(
            Constants.ServiceConfig.ENABLED_KEY,
            Constants.Defaults.DEFAULT_FEATURE_STATE
        );
        String apiKey = bundle.getString(
            Constants.ServiceConfig.API_KEY_KEY,
            Constants.Defaults.DEFAULT_API_KEY
        );
        ClientSideApphudConfig config = new ClientSideApphudConfig(
            enabled,
            apiKey
        );
        DebugLogger.INSTANCE.info(TAG, "received config on client side " + config);
        return config;
    }
}
