package io.appmetrica.analytics.impl.proxy;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig;
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler;

public class BaseAppMetricaProxy {

    @NonNull
    private final AppMetricaFacadeProvider mProvider;
    @NonNull
    private final ICommonExecutor mExecutor;
    @NonNull
    private final ActivationValidator mActivationValidator;
    @NonNull
    private final WebViewJsInterfaceHandler webViewJsInterfaceHandler;
    @NonNull
    private final ReporterProxyStorage mReporterProxyStorage;
    @NonNull
    private final DefaultOneShotMetricaConfig mDefaultOneShotConfig;

    @VisibleForTesting
    BaseAppMetricaProxy(@NonNull AppMetricaFacadeProvider provider,
                           @NonNull ICommonExecutor executor,
                           @NonNull ActivationValidator activationValidator,
                           @NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler,
                           @NonNull ReporterProxyStorage reporterProxyStorage,
                           @NonNull DefaultOneShotMetricaConfig defaultOneShotConfig) {
        mProvider = provider;
        mExecutor = executor;
        mActivationValidator = activationValidator;
        this.webViewJsInterfaceHandler = webViewJsInterfaceHandler;
        mReporterProxyStorage = reporterProxyStorage;
        mDefaultOneShotConfig = defaultOneShotConfig;
    }

    @NonNull
    ICommonExecutor getExecutor() {
        return mExecutor;
    }

    @NonNull
    AppMetricaFacadeProvider getProvider() {
        return mProvider;
    }

    @NonNull
    ActivationValidator getActivationValidator() {
        return mActivationValidator;
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
