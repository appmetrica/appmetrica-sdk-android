package io.appmetrica.analytics;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.proxy.ModulesProxy;
import io.appmetrica.analytics.logger.internal.YLogger;

/**
 * Class with methods for communication of different AppMetrica modules.
 */
public final class ModulesFacade {

    private static final String TAG = "[ModulesFacade]";

    @NonNull
    private static ModulesProxy proxy = new ModulesProxy(
        ClientServiceLocator.getInstance().getApiProxyExecutor()
    );

    /**
     * Reports custom event with various parameters.
     *
     * @param moduleEvent Description of event to send.
     */
    public static void reportEvent(@NonNull final ModuleEvent moduleEvent) {
        YLogger.info(TAG, "reportEvent: %s", moduleEvent);
        proxy.reportEvent(moduleEvent);
    }

    /**
     * Sets session extra.
     * This data is stored on disk and used for every event even after application stop.
     *
     * @param key {@link String} key
     * @param value Value of extra
     */
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

    /**
     * @return true if AppMetrica is activated and false otherwise.
     */
    public static boolean isActivatedForApp() {
        return proxy.isActivatedForApp();
    }

    /**
     * Initiates forced sending of all stored events from the buffer.
     * Sends all events even if the number of events is less than {@link AppMetricaConfig#maxReportsCount}.
     */
    public static void sendEventsBuffer() {
        proxy.sendEventsBuffer();
    }

    /**
     * @param context Context
     * @param apiKey API key of the required reporter
     * @return {@link IModuleReporter} for given API key
     */
    @NonNull
    public static IModuleReporter getModuleReporter(
        @NonNull final Context context,
        @NonNull final String apiKey
    ) {
        return proxy.getReporter(context, apiKey);
    }

    /**
     * Internal method for unit-tests.
     *
     * @param proxy Proxy object
     */
    @VisibleForTesting
    public static void setProxy(@NonNull final ModulesProxy proxy) {
        ModulesFacade.proxy = proxy;
    }
}
