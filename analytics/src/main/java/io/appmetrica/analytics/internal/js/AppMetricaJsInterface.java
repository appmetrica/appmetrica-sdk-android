package io.appmetrica.analytics.internal.js;

import android.webkit.JavascriptInterface;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;

/**
 * This class is used as a bridge between Javascript and native code.
 * It allows to report AppMetrica events directly from JS code.
 * For more info see method descriptions.
 */
public class AppMetricaJsInterface {

    private static final String TAG = "[AppMetricaJsInterface]";

    @NonNull
    private final AppMetricaProxy proxy;

    public AppMetricaJsInterface(@NonNull AppMetricaProxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Reports event with name and json value to AppMetrica from JS code.
     * Example of usage in JS:
     * <pre>
     * {@code
     *     function reportToAppMetrica() {
     *         // you can skip this check if you guarantee that it is always present
     *         if (typeof(AppMetrica) !== 'undefined') {
     *             AppMetrica.reportEvent("My name", "{}");
     *         }
     *     }
     * }
     * </pre>
     *
     * @param name event name
     * @param value event value in json format
     */
    @JavascriptInterface
    public void reportEvent(final String name, final String value) {
        YLogger.info(TAG, "report event with name: %s", name);
        proxy.reportJsEvent(name, value);
    }
}
