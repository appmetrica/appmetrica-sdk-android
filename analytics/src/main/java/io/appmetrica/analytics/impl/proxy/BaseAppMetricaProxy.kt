package io.appmetrica.analytics.impl.proxy

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler

internal open class BaseAppMetricaProxy protected constructor(
    protected val provider: AppMetricaFacadeProvider,
    protected val webViewJsInterfaceHandler: WebViewJsInterfaceHandler,
    protected val reporterProxyStorage: ReporterProxyStorage,
    protected val defaultOneShotConfig: DefaultOneShotMetricaConfig
) {

    protected val executor: ICommonExecutor =
        ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor

    protected fun dispatch(task: Runnable) {
        executor.execute(task)
    }

    protected fun dispatchSafely(task: Runnable) {
        executor.execute(object : SafeRunnable() {
            override fun runSafety() = task.run()
        })
    }
}
