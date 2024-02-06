package io.appmetrica.analytics.impl.proxy

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.IModuleReporter
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.impl.IMainReporter
import io.appmetrica.analytics.impl.proxy.synchronous.ModulesSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.ModulesBarrier

class ModulesProxy @VisibleForTesting constructor(
    private val executor: ICommonExecutor,
    private val provider: AppMetricaFacadeProvider,
    private val modulesBarrier: ModulesBarrier,
    private val synchronousStageExecutor: ModulesSynchronousStageExecutor,
) {
    private val TAG = "[ModulesProxy]"

    constructor(executor: ICommonExecutor) : this(
        executor,
        AppMetricaFacadeProvider()
    )

    private constructor(executor: ICommonExecutor, provider: AppMetricaFacadeProvider) : this(
        executor,
        provider,
        ModulesBarrier(provider),
        ModulesSynchronousStageExecutor()
    )

    fun reportEvent(
        moduleEvent: ModuleEvent
    ) {
        modulesBarrier.reportEvent(moduleEvent)
        synchronousStageExecutor.reportEvent(moduleEvent)

        executor.execute(object : SafeRunnable() {
            override fun runSafety() {
                getMainReporter().reportEvent(moduleEvent)
            }
        })
    }

    fun setSessionExtra(key: String, value: ByteArray?) {
        modulesBarrier.setSessionExtra(key, value)
        synchronousStageExecutor.setSessionExtra(key, value)

        executor.execute(object : SafeRunnable() {
            override fun runSafety() {
                getMainReporter().setSessionExtra(key, value)
            }
        })
    }

    fun isActivatedForApp(): Boolean {
        modulesBarrier.isActivatedForApp()
        synchronousStageExecutor.isActivatedForApp()
        return provider.isActivated
    }

    fun sendEventsBuffer() {
        modulesBarrier.sendEventsBuffer()
        synchronousStageExecutor.sendEventsBuffer()
        AppMetrica.sendEventsBuffer()
    }

    fun getReporter(context: Context, apiKey: String): IModuleReporter {
        modulesBarrier.getReporter(context, apiKey)
        synchronousStageExecutor.getReporter(context.applicationContext, apiKey)
        return ReporterProxyStorage.getInstance().getOrCreate(context.applicationContext, apiKey)
    }

    /*
    Use this method only after activationValidator.validate() to ensure mainReporter is not null
     */
    private fun getMainReporter(): IMainReporter =
        provider.peekInitializedImpl()!!.mainReporterApiConsumerProvider!!.mainReporter
}
