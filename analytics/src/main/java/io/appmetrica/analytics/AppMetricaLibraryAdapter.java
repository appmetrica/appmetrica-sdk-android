package io.appmetrica.analytics;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.proxy.AppMetricaLibraryAdapterProxy;

/**
 * Adapter for libraries that use AppMetrica.
 */
public class AppMetricaLibraryAdapter {

    @NonNull
    private static AppMetricaLibraryAdapterProxy proxy =
        new AppMetricaLibraryAdapterProxy();

    private AppMetricaLibraryAdapter() {}

    /**
     * Activate AppMetrica without API_KEY in anonymous mode.
     *
     * @param context {@link Context} object. Any application context.
     */
    public static void activate(@NonNull Context context) {
        proxy.activate(context);
    }

    /**
     * Activate AppMetrica without API_KEY in anonymous mode.
     *
     * @param context {@link Context} object. Any application context.
     * @param config {@link AppMetricaLibraryAdapterConfig} object.
     */
    public static void activate(@NonNull Context context, @NonNull AppMetricaLibraryAdapterConfig config) {
        proxy.activate(context, config);
    }

    /**
     * Enables or disables advertising identifiers tracking.
     *
     * @param enabled {@code true} to enable advanced identifiers tracking, {@code false} to disable it.
     */
    public static void setAdvIdentifiersTracking(boolean enabled) {
        proxy.setAdvIdentifiersTracking(enabled);
    }

    /**
     * Sends system report with provided data.
     *
     * @param sender {@link String} representation of sender.
     * @param event name of the event.
     * @param payload description of the event.
     */
    public static void reportEvent(
        @NonNull String sender,
        @NonNull String event,
        @NonNull String payload
    ) {
        proxy.reportEvent(sender, event, payload);
    }

    /**
     * Subscribes for auto-collected data flow.
     *
     * @param context {@link Context} object. Any application context.
     * @param apiKey AppMetrica API_KEY.
     */
    public static void subscribeForAutoCollectedData(@NonNull Context context, @NonNull String apiKey) {
        proxy.subscribeForAutoCollectedData(context, apiKey);
    }

    /**
     * Internal method for unit-tests.
     *
     * @param proxy Proxy object
     */
    @VisibleForTesting
    public static void setProxy(@NonNull AppMetricaLibraryAdapterProxy proxy) {
        AppMetricaLibraryAdapter.proxy = proxy;
    }
}
