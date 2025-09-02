package io.appmetrica.analytics.impl.proxy

import android.content.Context
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

class ModulesProxy {
    private val provider: AppMetricaFacadeProvider = ClientServiceLocator.getInstance().appMetricaFacadeProvider
    private val modulesBarrier = ModulesBarrier(provider)
    private val synchronousStageExecutor = ModulesSynchronousStageExecutor()
    private val executor: ICommonExecutor = ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor

    fun setAdvIdentifiersTracking(enabled: Boolean) {
        modulesBarrier.setAdvIdentifiersTracking(enabled)
        synchronousStageExecutor.setAdvIdentifiersTracking(enabled)

        executor.execute(
            object : SafeRunnable() {
                override fun runSafety() {
                    getMainReporter().setAdvIdentifiersTracking(enabled)
                }
            }
        )
    }

    fun reportEvent(moduleEvent: ModuleEvent) {
        modulesBarrier.reportEvent(moduleEvent)
        synchronousStageExecutor.reportEvent(moduleEvent)

        executor.execute(
            object : SafeRunnable() {
                override fun runSafety() {
                    getMainReporter().reportEvent(moduleEvent)
                }
            }
        )
    }

    fun setSessionExtra(
        key: String,
        value: ByteArray?
    ) {
        modulesBarrier.setSessionExtra(key, value)
        synchronousStageExecutor.setSessionExtra(key, value)

        executor.execute(
            object : SafeRunnable() {
                override fun runSafety() {
                    getMainReporter().setSessionExtra(key, value)
                }
            }
        )
    }

    fun reportExternalAttribution(
        source: Int,
        value: String?
    ) {
        modulesBarrier.reportExternalAttribution(source, value)
        synchronousStageExecutor.reportExternalAttribution(source, value)

        executor.execute(
            object : SafeRunnable() {
                override fun runSafety() {
                    getMainReporter().reportExternalAttribution(ExternalAttributionFromModule(source, value))
                }
            }
        )
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

    fun getReporter(
        context: Context,
        apiKey: String
    ): IModuleReporter {
        modulesBarrier.getReporter(context, apiKey)
        synchronousStageExecutor.getReporter(context.applicationContext, apiKey)
        return ReporterProxyStorage.getInstance().getOrCreate(context.applicationContext, apiKey)
    }

    fun reportAdRevenue(
        adRevenue: AdRevenue,
        autoCollected: Boolean
    ) {
        modulesBarrier.reportAdRevenue(adRevenue, autoCollected)
        synchronousStageExecutor.reportAdRevenue(adRevenue, autoCollected)

        executor.execute(
            object : SafeRunnable() {
                override fun runSafety() {
                    getMainReporter().reportAdRevenue(adRevenue, autoCollected)
                }
            }
        )
    }

    fun subscribeForAutoCollectedData(context: Context, apiKey: String) {
        modulesBarrier.subscribeForAutoCollectedData(context, apiKey)
        synchronousStageExecutor.subscribeForAutoCollectedData(context, apiKey)

        executor.execute(
            object : SafeRunnable() {
                override fun runSafety() {
                    ClientServiceLocator.getInstance().appMetricaFacadeProvider.addAutoCollectedDataSubscriber(apiKey)
                }
            }
        )
    }

    /*
    Use this method only after activationValidator.validate() to ensure mainReporter is not null
     */
    private fun getMainReporter(): IMainReporter =
        provider.peekInitializedImpl()!!.mainReporterApiConsumerProvider!!.mainReporter
}
