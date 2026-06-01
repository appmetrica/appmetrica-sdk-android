package io.appmetrica.analytics.apphud.impl.config.service;

import android.os.Bundle;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.service.model.ServiceSideApphudConfig;

public class ServiceSideApphudConfigToBundleConverter {

    @Nullable
    public Bundle convert(@Nullable ServiceSideApphudConfig config) {
        if (config == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.ServiceConfig.ENABLED_KEY, config.isEnabled());
        bundle.putString(Constants.ServiceConfig.API_KEY_KEY, config.getApiKey());

        return bundle;
    }
}
