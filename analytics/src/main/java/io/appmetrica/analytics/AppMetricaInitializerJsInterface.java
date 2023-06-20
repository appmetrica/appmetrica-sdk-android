package io.appmetrica.analytics;

import android.webkit.JavascriptInterface;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;

public class AppMetricaInitializerJsInterface {

    private static final String TAG = "[AppMetricaInitializerJsInterface]";

    @NonNull
    private final AppMetricaProxy proxy;

    public AppMetricaInitializerJsInterface(@NonNull AppMetricaProxy proxy) {
        this.proxy = proxy;
    }

    @JavascriptInterface
    public void init(final String value) {
        YLogger.info(TAG, "init");
        proxy.reportJsInitEvent(value);
    }
}
