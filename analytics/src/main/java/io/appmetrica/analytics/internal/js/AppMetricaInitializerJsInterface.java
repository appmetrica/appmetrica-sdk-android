package io.appmetrica.analytics.internal.js;

import android.webkit.JavascriptInterface;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;
import io.appmetrica.analytics.logger.internal.DebugLogger;

/**
 * This class is used as a bridge between JavaScript and native code.
 * It allows to report AppMetrica JacaScript init event directly from JS code.
 * For more info see method descriptions.
 */
public class AppMetricaInitializerJsInterface {

    private static final String TAG = "[AppMetricaInitializerJsInterface]";

    @NonNull
    private final AppMetricaProxy proxy;

    public AppMetricaInitializerJsInterface(@NonNull AppMetricaProxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Send JS init event.
     *
     * @param value value for JS init event.
     */
    @JavascriptInterface
    public void init(final String value) {
        DebugLogger.info(TAG, "init");
        proxy.reportJsInitEvent(value);
    }
}
