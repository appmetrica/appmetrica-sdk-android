package io.appmetrica.analytics.impl.proxy

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.IModuleReporter
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.IMainReporter
import io.appmetrica.analytics.impl.proxy.validation.ModulesBarrier

class ModulesProxy @VisibleForTesting constructor(
    private val executor: ICommonExecutor,
    private val provider: AppMetricaFacadeProvider,
    private val modulesBarrier: ModulesBarrier,
) {
    private val TAG = "[ModulesProxy]"

    constructor(executor: ICommonExecutor) : this(
        executor,
        AppMetricaFacadeProvider()
    )

    private constructor(executor: ICommonExecutor, provider: AppMetricaFacadeProvider) : this(
        executor,
        provider,
        ModulesBarrier(provider)
    )

    fun reportEvent(
        moduleEvent: ModuleEvent
    ) {
        modulesBarrier.reportEvent(moduleEvent)

        executor.execute(object : SafeRunnable() {
            override fun runSafety() {
                getMainReporter().reportEvent(moduleEvent)
            }
        })
    }

    fun setSessionExtra(key: String, value: ByteArray?) {
        modulesBarrier.setSessionExtra(key, value)

        executor.execute(object : SafeRunnable() {
            override fun runSafety() {
                getMainReporter().setSessionExtra(key, value)
            }
        })
    }

    fun isActivatedForApp(): Boolean {
        modulesBarrier.isActivatedForApp()
        return provider.isActivated
    }

    fun sendEventsBuffer() {
        modulesBarrier.sendEventsBuffer()
        AppMetrica.sendEventsBuffer()
    }

    fun getReporter(context: Context, apiKey: String): IModuleReporter {
        modulesBarrier.getReporter(context, apiKey)
        ClientServiceLocator.getInstance().contextAppearedListener.onProbablyAppeared(context.applicationContext)
        return ReporterProxyStorage.getInstance().getOrCreate(context.applicationContext, apiKey)
    }

    /*
    Use this method only after activationValidator.validate() to ensure mainReporter is not null
     */
    private fun getMainReporter(): IMainReporter =
        provider.peekInitializedImpl()!!.mainReporterApiConsumerProvider!!.mainReporter
}
