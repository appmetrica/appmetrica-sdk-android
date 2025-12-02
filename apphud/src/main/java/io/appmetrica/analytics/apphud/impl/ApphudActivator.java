package io.appmetrica.analytics.apphud.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.config.client.ClientApphudConfig;
import io.appmetrica.analytics.apphud.impl.config.client.ClientApphudConfigChecker;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class ApphudActivator {

    private static final String TAG = "[ApphudActivator]";

    private boolean activated = false;

    @NonNull
    private final ClientApphudConfigChecker configChecker;

    public ApphudActivator(
        @NonNull ClientApphudConfigChecker configChecker
    ) {
        this.configChecker = configChecker;
    }

    public synchronized void activateIfNecessary(
        @NonNull Context context,
        @NonNull ClientApphudConfig config
    ) {
        if (activated) {
            DebugLogger.INSTANCE.info(TAG, "Apphud has already been activated");
            return;
        }
        if (configChecker.doesNeedUpdate(config)) {
            DebugLogger.INSTANCE.info(TAG, "Apphud is not activated since config '" + config + "' needs update");
            return;
        }
        DebugLogger.INSTANCE.info(TAG, "Activating Apphud with config = " + config);
        if (config.getApiKey() != null) { // checked by configChecker.doesNeedUpdate(config)
            ApphudWrapperProvider.getApphudWrapper().start(
                context,
                config.getApiKey(),
                config.getUuid(),
                config.getDeviceId(),
                true
            );
        }
        activated = true;
        DebugLogger.INSTANCE.info(TAG, "Apphud is activated");
    }
}
