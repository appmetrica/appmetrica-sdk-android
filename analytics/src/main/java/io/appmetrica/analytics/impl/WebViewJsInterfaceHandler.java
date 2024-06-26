package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.internal.js.AppMetricaInitializerJsInterface;
import io.appmetrica.analytics.internal.js.AppMetricaJsInterface;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.ArrayList;
import java.util.List;

public class WebViewJsInterfaceHandler {

    private static final String TAG = "[WebViewJsInterfaceHandler]";
    private static final String APPMETRICA_INTERFACE_NAME = "AppMetrica";
    private static final String APPMETRICA_INITIALIZER_INTERFACE_NAME = "AppMetricaInitializer";

    @NonNull
    private final List<Consumer<PublicLogger>> queuedLogMessages = new ArrayList<Consumer<PublicLogger>>();
    @Nullable
    private PublicLogger publicLogger;

    @SuppressLint("AddJavascriptInterface")
    public void initWebViewReporting(@NonNull WebView webView, @NonNull AppMetricaProxy proxy) {
        try {
            if (webView.getSettings().getJavaScriptEnabled()) {
                webView.addJavascriptInterface(new AppMetricaJsInterface(proxy), APPMETRICA_INTERFACE_NAME);
                webView.addJavascriptInterface(
                    new AppMetricaInitializerJsInterface(proxy),
                    APPMETRICA_INITIALIZER_INTERFACE_NAME
                );
                logIOrQueue("WebView interface setup is successful.");
            } else {
                logWOrQueue("WebView interface setup failed because javascript is disabled for the WebView.");
            }
        } catch (Throwable ex) {
            DebugLogger.INSTANCE.error(TAG, ex);
            logEOrQueue("WebView interface setup failed because of an exception.", ex);
        }
    }

    public void setLogger(@NonNull PublicLogger logger) {
        synchronized (this) {
            publicLogger = logger;
        }
        for (Consumer<PublicLogger> queuedMessage : queuedLogMessages) {
            queuedMessage.consume(logger);
        }
        queuedLogMessages.clear();
    }

    private void logIOrQueue(@NonNull final String message) {
        logOrQueue(new Consumer<PublicLogger>() {
            @Override
            public void consume(PublicLogger input) {
                input.info(message);
            }
        });
    }

    private void logWOrQueue(@NonNull final String message) {
        logOrQueue(new Consumer<PublicLogger>() {
            @Override
            public void consume(PublicLogger input) {
                input.warning(message);
            }
        });
    }

    private void logEOrQueue(@NonNull final String message, @NonNull final Throwable ex) {
        logOrQueue(new Consumer<PublicLogger>() {
            @Override
            public void consume(PublicLogger input) {
                input.error(ex, message);
            }
        });
    }

    private synchronized void logOrQueue(@NonNull Consumer<PublicLogger> message) {
        if (publicLogger == null) {
            queuedLogMessages.add(message);
        } else {
            message.consume(publicLogger);
        }
    }
}
