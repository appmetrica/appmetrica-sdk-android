package io.appmetrica.analytics.billing.impl

import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideRemoteBillingConfig
import io.appmetrica.analytics.billing.impl.sender.BillingInfoSenderImpl
import io.appmetrica.analytics.billing.impl.storage.AutoInappCollectingInfoConverter
import io.appmetrica.analytics.billing.impl.storage.AutoInappCollectingInfoSerializer
import io.appmetrica.analytics.billing.impl.storage.BillingInfoStorageImpl
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.monitor.BillingMonitor
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentContext
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentLifecycleListener
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

class BillingMonitorWrapper(
    private val serviceContext: ServiceContext,
    private var config: ServiceSideRemoteBillingConfig?
) : ServiceModuleReporterComponentLifecycleListener {

    private val tag = "[BillingMonitorWrapper]"

    private var billingMonitor: BillingMonitor? = null

    fun updateConfig(config: ServiceSideRemoteBillingConfig?) {
        this.config = config.also {
            billingMonitor?.onBillingConfigChanged(config?.toBillingConfig())
        }
    }

    override fun onMainReporterCreated(context: ServiceModuleReporterComponentContext) {
        if (!context.config.isRevenueAutoTrackingEnabled()) {
            return
        }

        billingMonitor = BillingMonitorProvider().get(
            serviceContext.context,
            serviceContext.executorProvider.getDefaultExecutor(),
            serviceContext.executorProvider.getUiExecutor(),
            BillingTypeDetector.getBillingType(),
            BillingInfoStorageImpl(
                serviceContext.serviceStorageProvider.createBinaryStateStorageFactory(
                    key = Constants.Storage.STORAGE_KEY,
                    serializer = AutoInappCollectingInfoSerializer(),
                    converter = AutoInappCollectingInfoConverter()
                ).create(serviceContext.context)
            ),
            BillingInfoSenderImpl(
                context.reporter,
                serviceContext.executorProvider.getReportRunnableExecutor(),
            )
        )

        updateConfig(config)

        val currentState = serviceContext.applicationStateProvider.registerStickyObserver { state ->
            checkStateAndCollectAutoInapp(state)
        }
        checkStateAndCollectAutoInapp(currentState)
    }

    private fun checkStateAndCollectAutoInapp(state: ApplicationState) {
        DebugLogger.info(tag, "checkStateAndCollectAutoInapp ${state.stringValue}")
        if (state == ApplicationState.VISIBLE) {
            try {
                val billingMonitorCopy = billingMonitor
                billingMonitorCopy?.onSessionResumed()
            } catch (e: Throwable) {
                DebugLogger.error(tag, "Error occurred during billing library call $e")
            }
        }
    }

    private fun ServiceSideRemoteBillingConfig.toBillingConfig() = BillingConfig(
        config.sendFrequencySeconds,
        config.firstCollectingInappMaxAgeSeconds
    )
}
