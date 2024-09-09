package io.appmetrica.analytics.apphud.impl.config.remote;

import android.os.Bundle;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.Constants;

public class RemoteApphudConfigBundleConverter {

    @Nullable
    public Bundle convert(@Nullable RemoteApphudConfig config) {
        if (config == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.ServiceConfig.ENABLED_KEY, config.isEnabled());
        bundle.putString(Constants.ServiceConfig.API_KEY_KEY, config.getApiKey());

        return bundle;
    }
}
