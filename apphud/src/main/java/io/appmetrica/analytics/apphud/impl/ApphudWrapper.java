package io.appmetrica.analytics.apphud.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.apphud.sdk.Apphud;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class ApphudWrapper {

    private static final String TAG = "[ApphudWrapper]";

    private ApphudWrapper() {}

    public static void start(
        @NonNull Context context,
        @NonNull String apiKey,
        @Nullable String uuid,
        @Nullable String deviceId,
        boolean observerMode
    ) {
        try {
            Apphud.INSTANCE.start(
                context,
                apiKey,
                uuid,
                deviceId,
                observerMode,
                apphudUser -> null
            );
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e, e.getMessage());
        }
    }
}
