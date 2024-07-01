package io.appmetrica.analytics.impl.proxy

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.IModuleReporter
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.IMainReporter
import io.appmetrica.analytics.impl.attribution.ExternalAttributionFromModule
import io.appmetrica.analytics.impl.proxy.synchronous.ModulesSynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.ModulesBarrier

class ModulesProxy @VisibleForTesting constructor(
    private val provider: AppMetricaFacadeProvider,
    private val modulesBarrier: ModulesBarrier,
    private val synchronousStageExecutor: ModulesSynchronousStageExecutor,
) {

    private val executor: ICommonExecutor = ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor

    constructor() : this(
        AppMetricaFacadeProvider()
    )

    private constructor(provider: AppMetricaFacadeProvider) : this(
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

    fun reportExternalAttribution(source: Int, value: String?) {
        modulesBarrier.reportExternalAttribution(source, value)
        synchronousStageExecutor.reportExternalAttribution(source, value)

        executor.execute(object : SafeRunnable() {
            override fun runSafety() {
                getMainReporter().reportExternalAttribution(ExternalAttributionFromModule(source, value))
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

    fun reportAdRevenue(adRevenue: AdRevenue, autoCollected: Boolean) {
        modulesBarrier.reportAdRevenue(adRevenue, autoCollected)
        synchronousStageExecutor.reportAdRevenue(adRevenue, autoCollected)

        executor.execute(object : SafeRunnable() {
            override fun runSafety() {
                getMainReporter().reportAdRevenue(adRevenue, autoCollected)
            }
        })
    }

    /*
    Use this method only after activationValidator.validate() to ensure mainReporter is not null
     */
    private fun getMainReporter(): IMainReporter =
        provider.peekInitializedImpl()!!.mainReporterApiConsumerProvider!!.mainReporter
}
