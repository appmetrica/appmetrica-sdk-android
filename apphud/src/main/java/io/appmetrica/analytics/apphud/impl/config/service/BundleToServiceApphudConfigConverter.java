package io.appmetrica.analytics.apphud.impl.config.service;

import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.BundleToServiceConfigConverter;

public class BundleToServiceApphudConfigConverter implements BundleToServiceConfigConverter<ServiceApphudConfig> {

    private static final String TAG = "[ServiceModuleConfigBundleParser]";

    @NonNull
    @Override
    public ServiceApphudConfig fromBundle(@NonNull Bundle bundle) {
        boolean enabled = bundle.getBoolean(
            Constants.ServiceConfig.ENABLED_KEY,
            Constants.Defaults.DEFAULT_FEATURE_STATE
        );
        String apiKey = bundle.getString(
            Constants.ServiceConfig.API_KEY_KEY,
            Constants.Defaults.DEFAULT_API_KEY
        );
        ServiceApphudConfig config = new ServiceApphudConfig(
            enabled,
            apiKey
        );
        DebugLogger.INSTANCE.info(TAG, "received config on client side " + config);
        return config;
    }
}
