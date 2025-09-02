package io.appmetrica.analytics;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution;
import io.appmetrica.analytics.impl.proxy.ModulesProxy;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

/**
 * Class with methods for communication of different AppMetrica modules.
 */
public final class ModulesFacade {

    /**
     * Attribution source "Appsflyer SDK" for {@link ModulesFacade#reportExternalAttribution(int, String)}.
     */
    public static final int EXTERNAL_ATTRIBUTION_APPSFLYER = ExternalAttribution.ClientExternalAttribution.APPSFLYER;
    /**
     * Attribution source "Adjust SDK" for {@link ModulesFacade#reportExternalAttribution(int, String)}.
     */
    public static final int EXTERNAL_ATTRIBUTION_ADJUST = ExternalAttribution.ClientExternalAttribution.ADJUST;
    /**
     * Attribution source "Kochava SDK" for {@link ModulesFacade#reportExternalAttribution(int, String)}.
     */
    public static final int EXTERNAL_ATTRIBUTION_KOCHAVA = ExternalAttribution.ClientExternalAttribution.KOCHAVA;
    /**
     * Attribution source "Tenjin SDK" for {@link ModulesFacade#reportExternalAttribution(int, String)}.
     */
    public static final int EXTERNAL_ATTRIBUTION_TENJIN = ExternalAttribution.ClientExternalAttribution.TENJIN;
    /**
     * Attribution source "Airbridge SDK" for {@link ModulesFacade#reportExternalAttribution(int, String)}.
     */
    public static final int EXTERNAL_ATTRIBUTION_AIRBRIDGE = ExternalAttribution.ClientExternalAttribution.AIRBRIDGE;
    /**
     * Attribution source "Singular SDK" for {@link ModulesFacade#reportExternalAttribution(int, String)}.
     */
    public static final int EXTERNAL_ATTRIBUTION_SINGULAR = ExternalAttribution.ClientExternalAttribution.SINGULAR;

    private static final String TAG = "[ModulesFacade]";

    @NonNull
    private static ModulesProxy proxy = new ModulesProxy();

    /**
     * Enables/disables including advertising identifiers like GAID, Huawei OAID within its reports.
     *
     * @param enabled {@code true} to allow AppMetrica to record advertising identifiers information in reports,
     *                            otherwise {@code false}.
     *
     * @see AppMetricaConfig.Builder#withAdvIdentifiersTracking(boolean)
     * @see AppMetricaConfig#advIdentifiersTracking
     */
    public static void setAdvIdentifiersTracking(boolean enabled) {
        DebugLogger.INSTANCE.info(TAG, "setAdvIdentifiersTracking: %s", enabled);
        proxy.setAdvIdentifiersTracking(enabled);
    }

    /**
     * Reports custom event with various parameters.
     *
     * @param moduleEvent Description of event to send.
     */
    public static void reportEvent(@NonNull final ModuleEvent moduleEvent) {
        DebugLogger.INSTANCE.info(TAG, "reportEvent: %s", moduleEvent);
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
        DebugLogger.INSTANCE.info(
            TAG,
            "setSessionExtra with key = `%s` and value size: %s",
            key,
            value != null ? value.length : null
        );
        proxy.setSessionExtra(key, value);
    }

    /**
     * Reports attribution from external library.
     *
     * @param source the SDK that provided the attribution.
     *               One of
     *               {@link ModulesFacade#EXTERNAL_ATTRIBUTION_APPSFLYER},
     *               {@link ModulesFacade#EXTERNAL_ATTRIBUTION_ADJUST},
     *               {@link ModulesFacade#EXTERNAL_ATTRIBUTION_KOCHAVA},
     *               {@link ModulesFacade#EXTERNAL_ATTRIBUTION_TENJIN},
     *               {@link ModulesFacade#EXTERNAL_ATTRIBUTION_AIRBRIDGE},
     *               {@link ModulesFacade#EXTERNAL_ATTRIBUTION_SINGULAR}
     *
     * @param value the attribution value.
     */
    public static void reportExternalAttribution(int source, @NonNull String value) {
        proxy.reportExternalAttribution(source, value);
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
     * Reports auto collected adRevenue to AppMetrica
     *
     * @param adRevenue AdRevenue
     */
    public static void reportAdRevenue(@NonNull AdRevenue adRevenue) {
        reportAdRevenue(adRevenue, true);
    }

    /**
     * Reports adRevenue to AppMetrica
     *
     * @param adRevenue AdRevenue
     * @param autoCollected whether AdRevenue collected automatically
     */
    public static void reportAdRevenue(@NonNull AdRevenue adRevenue, @NonNull Boolean autoCollected) {
        proxy.reportAdRevenue(adRevenue, autoCollected);
    }

    /** Subscribe for auto collected data.
     *
     * @param context Context object. Any application context.
     * @param apiKey API key of the required reporter
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
    public static void setProxy(@NonNull final ModulesProxy proxy) {
        ModulesFacade.proxy = proxy;
    }
}
