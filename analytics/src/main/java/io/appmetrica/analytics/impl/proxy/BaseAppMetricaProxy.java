package io.appmetrica.analytics.impl.proxy;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.ContextAppearedListener;
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig;
import io.appmetrica.analytics.impl.SynchronousStageExecutor;
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler;

public class BaseAppMetricaProxy {

    @NonNull
    private final AppMetricaFacadeProvider mProvider;
    @NonNull
    private final ICommonExecutor mExecutor;
    @NonNull
    private final ActivationValidator mActivationValidator;
    @NonNull
    private final SynchronousStageExecutor mSynchronousStageExecutor;
    @NonNull
    private final WebViewJsInterfaceHandler webViewJsInterfaceHandler;
    @NonNull
    private final ReporterProxyStorage mReporterProxyStorage;
    @NonNull
    private final DefaultOneShotMetricaConfig mDefaultOneShotConfig;
    @NonNull
    private final ContextAppearedListener contextAppearedListener;

    @VisibleForTesting
    BaseAppMetricaProxy(@NonNull AppMetricaFacadeProvider provider,
                           @NonNull ICommonExecutor executor,
                           @NonNull ActivationValidator activationValidator,
                           @NonNull WebViewJsInterfaceHandler webViewJsInterfaceHandler,
                           @NonNull SynchronousStageExecutor synchronousStageExecutor,
                           @NonNull ReporterProxyStorage reporterProxyStorage,
                           @NonNull DefaultOneShotMetricaConfig defaultOneShotConfig,
                           @NonNull ContextAppearedListener contextAppearedListener) {
        mProvider = provider;
        mExecutor = executor;
        mActivationValidator = activationValidator;
        this.webViewJsInterfaceHandler = webViewJsInterfaceHandler;
        mSynchronousStageExecutor = synchronousStageExecutor;
        mReporterProxyStorage = reporterProxyStorage;
        mDefaultOneShotConfig = defaultOneShotConfig;
        this.contextAppearedListener = contextAppearedListener;
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
    SynchronousStageExecutor getSynchronousStageExecutor() {
        return mSynchronousStageExecutor;
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

    @NonNull
    ContextAppearedListener getContextAppearedListener() {
        return contextAppearedListener;
    }
}
