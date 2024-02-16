package io.appmetrica.analytics.impl.proxy

import android.util.Log
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.IMainReporter
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.impl.proxy.synchronous.PluginsSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.PluginsBarrier
import io.appmetrica.analytics.plugins.PluginErrorDetails

internal class AppMetricaPluginsProxy @VisibleForTesting constructor(
    private val executor: ICommonExecutor,
    private val provider: AppMetricaFacadeProvider,
    private val barrier: PluginsBarrier,
    private val synchronousStageExecutor: PluginsSynchronousStageExecutor
) {

    constructor(executor: ICommonExecutor) : this(
        executor,
        AppMetricaFacadeProvider()
    )

    private constructor(executor: ICommonExecutor, provider: AppMetricaFacadeProvider) : this(
        executor,
        provider,
        PluginsBarrier(provider),
        PluginsSynchronousStageExecutor()
    )

    fun reportUnhandledException(errorDetails: PluginErrorDetails?) {
        barrier.reportUnhandledException(errorDetails)
        // objects are non-null because otherwise validation would have failed
        synchronousStageExecutor.reportPluginUnhandledException(errorDetails!!)
        executor.execute {
            getMainReporter().pluginExtension.reportUnhandledException(errorDetails)
        }
    }

    fun reportError(errorDetails: PluginErrorDetails?, message: String?) {
        if (!barrier.reportErrorWithFilledStacktrace(errorDetails, message)) {
            Log.w(SdkUtils.APPMETRICA_TAG, "Error stacktrace must be non empty")
            return
        }
        // objects are non-null because otherwise validation would have failed
        synchronousStageExecutor.reportPluginError(errorDetails!!, message)
        executor.execute {
            getMainReporter().pluginExtension.reportError(errorDetails, message)
        }
    }

    fun reportError(identifier: String?, message: String?, errorDetails: PluginErrorDetails?) {
        barrier.reportError(identifier, message, errorDetails)
        // objects are non-null because otherwise validation would have failed
        synchronousStageExecutor.reportPluginError(identifier!!, message, errorDetails)
        executor.execute {
            getMainReporter().pluginExtension.reportError(identifier, message, errorDetails)
        }
    }

    private fun getMainReporter(): IMainReporter =
        provider.peekInitializedImpl()!!.mainReporterApiConsumerProvider!!.mainReporter
}
