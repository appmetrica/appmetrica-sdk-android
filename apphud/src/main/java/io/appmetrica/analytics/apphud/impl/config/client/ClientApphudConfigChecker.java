package io.appmetrica.analytics.apphud.impl.config.client;

import android.text.TextUtils;
import androidx.annotation.Nullable;

public class ClientApphudConfigChecker {

    public boolean doesNeedUpdate(@Nullable ClientApphudConfig config) {
        return config == null ||
            TextUtils.isEmpty(config.getApiKey()) ||
            TextUtils.isEmpty(config.getDeviceId()) ||
            TextUtils.isEmpty(config.getUuid());
    }
}
