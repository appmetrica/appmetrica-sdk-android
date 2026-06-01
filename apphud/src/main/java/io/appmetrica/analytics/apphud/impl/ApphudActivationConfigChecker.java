package io.appmetrica.analytics.apphud.impl;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.config.client.model.ApphudActivationConfig;

public class ApphudActivationConfigChecker {

    public boolean doesNeedUpdate(@Nullable ApphudActivationConfig config) {
        return config == null ||
            TextUtils.isEmpty(config.getApiKey()) ||
            TextUtils.isEmpty(config.getDeviceId()) ||
            TextUtils.isEmpty(config.getUuid());
    }
}
