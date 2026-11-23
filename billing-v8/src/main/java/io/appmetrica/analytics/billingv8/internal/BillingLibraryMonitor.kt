package io.appmetrica.analytics.billingv8.internal

import android.content.Context
import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billinginterface.internal.monitor.BillingMonitor
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy
import io.appmetrica.analytics.billingv8.impl.Constants.MODULE_TAG
import io.appmetrica.analytics.billingv8.impl.library.BillingConnectionController
import io.appmetrica.analytics.billingv8.impl.library.BillingPurchaseCollector
import io.appmetrica.analytics.billingv8.impl.library.PurchasesUpdatedListenerImpl
import io.appmetrica.analytics.billingv8.impl.storage.BillingInfoManagerImpl
import io.appmetrica.analytics.billingv8.impl.update.UpdatePolicyImpl
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.Executor

class BillingLibraryMonitor(
    private val context: Context,
    private val workerExecutor: IHandlerExecutor,
    private val uiExecutor: Executor,
    private val billingInfoStorage: BillingInfoStorage,
    private val billingInfoSender: BillingInfoSender,
    private val billingInfoManager: BillingInfoManager = BillingInfoManagerImpl(billingInfoStorage),
    private val updatePolicy: UpdatePolicy = UpdatePolicyImpl(),
) : BillingMonitor {

    private var billingConfig: BillingConfig? = null

    @WorkerThread
    override fun onSessionResumed() {
        DebugLogger.info(MODULE_TAG, "onSessionResumed")
        billingConfig?.let(connectionController::request)
    }

    @WorkerThread
    override fun onBillingConfigChanged(billingConfig: BillingConfig?) {
        if (this.billingConfig != billingConfig) {
            DebugLogger.info(MODULE_TAG, "onBillingConfigChanged: $billingConfig")
            this.billingConfig = billingConfig
            if (billingConfig == null) {
                connectionController.disable()
            } else {
                connectionController.request(billingConfig)
            }
        }
    }

    private fun createClient() = BillingClient.newBuilder(context)
        .setListener(PurchasesUpdatedListenerImpl())
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private val utilsProvider = object : UtilsProvider {
        override fun getBillingInfoManager() = this@BillingLibraryMonitor.billingInfoManager

        override fun getUpdatePolicy() = this@BillingLibraryMonitor.updatePolicy

        override fun getBillingInfoSender() = this@BillingLibraryMonitor.billingInfoSender

        override fun getUiExecutor() = this@BillingLibraryMonitor.uiExecutor

        override fun getWorkerExecutor() = this@BillingLibraryMonitor.workerExecutor
    }

    private val connectionController = BillingConnectionController(
        workerExecutor = workerExecutor,
        createClient = ::createClient,
        collect = BillingPurchaseCollector(utilsProvider)::collect,
    )
}
