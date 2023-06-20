package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues;
import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.networktasks.internal.ScreenInfoProvider;

public class ScreenInfoHolder implements ScreenInfoProvider {

    private static final String TAG = "[ScreenInfoHolder]";

    @NonNull
    private ScreenInfo screenInfo;

    public ScreenInfoHolder() {
        screenInfo = new ScreenInfo(0, 0, 0, 0f, DeviceTypeValues.PHONE);
    }

    public synchronized void maybeUpdateInfo(@Nullable ScreenInfo newInfo) {
        YLogger.info(TAG, "maybeUpdateInfo: %s", newInfo);
        if (newInfo != null) {
            screenInfo = newInfo;
        }
    }

    @Override
    @NonNull
    public synchronized ScreenInfo getScreenInfo() {
        return screenInfo;
    }
}
