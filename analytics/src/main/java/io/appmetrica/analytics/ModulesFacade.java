package io.appmetrica.analytics;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.proxy.ModulesProxy;

public final class ModulesFacade {

    private static final String TAG = "[ModulesFacade]";

    @NonNull
    private static ModulesProxy proxy = new ModulesProxy(
        ClientServiceLocator.getInstance().getApiProxyExecutor()
    );

    public static void reportEvent(@NonNull final ModuleEvent moduleEvent) {
        YLogger.info(TAG, "reportEvent: %s", moduleEvent);
        proxy.reportEvent(moduleEvent);
    }

    public static void setSessionExtra(
        @NonNull final String key,
        @Nullable final byte[] value
    ) {
        YLogger.info(
            TAG,
            "setSessionExtra with key = `%s` and value size: %s",
            key,
            value != null ? value.length : null
        );
        proxy.setSessionExtra(key, value);
    }

    public static boolean isActivatedForApp() {
        return proxy.isActivatedForApp();
    }

    public static void sendEventsBuffer() {
        proxy.sendEventsBuffer();
    }

    @NonNull
    public static IModuleReporter getModuleReporter(
        @NonNull final Context context,
        @NonNull final String apiKey
    ) {
        return proxy.getReporter(context, apiKey);
    }

    @VisibleForTesting
    public static void setProxy(@NonNull final ModulesProxy proxy) {
        ModulesFacade.proxy = proxy;
    }
}
