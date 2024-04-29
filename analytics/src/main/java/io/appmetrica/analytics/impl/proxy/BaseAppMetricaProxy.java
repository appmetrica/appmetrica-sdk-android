package io.appmetrica.analytics.impl.proxy;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig;
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler;

public class BaseAppMetricaProxy {

    @NonNull
    private final AppMetricaFacadeProvider mProvider;
    @NonNull
    private final WebViewJsInterfaceHandler webViewJsInterfaceHandler;
    @NonNull
    private final ReporterProxyStorage mReporterProxyStorage;
    @NonNull
    private final DefaultOneShotMetricaConfig mDefaultOneShotConfig;

    @VisibleForTesting
    BaseAppMetricaProxy(@NonNull AppMetricaFacadeProvider provider,
                           @NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler,
                           @NonNull ReporterProxyStorage reporterProxyStorage,
                           @NonNull DefaultOneShotMetricaConfig defaultOneShotConfig) {
        mProvider = provider;
        this.webViewJsInterfaceHandler = webViewJsInterfaceHandler;
        mReporterProxyStorage = reporterProxyStorage;
        mDefaultOneShotConfig = defaultOneShotConfig;
    }

    @NonNull
    ICommonExecutor getExecutor() {
        return ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor();
    }

    @NonNull
    AppMetricaFacadeProvider getProvider() {
        return mProvider;
    }

    @NonNull
    ReporterProxyStorage getReporterProxyStorage() {
        return mReporterProxyStorage;
    }

    @NonNull
    DefaultOneShotMetricaConfig getDefaultOneShotConfig() {
        return mDefaultOneShotConfig;
    }

    @NonNull
    WebViewJsInterfaceHandler getWebViewJsInterfaceHandler() {
        return webViewJsInterfaceHandler;
    }
}
