package io.appmetrica.analytics.billingv6.internal

import android.content.Context
import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billinginterface.internal.monitor.BillingMonitor
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy
import io.appmetrica.analytics.billingv6.impl.MODULE_TAG
import io.appmetrica.analytics.billingv6.impl.library.BillingClientStateListenerImpl
import io.appmetrica.analytics.billingv6.impl.library.PurchasesUpdatedListenerImpl
import io.appmetrica.analytics.billingv6.impl.storage.BillingInfoManagerImpl
import io.appmetrica.analytics.billingv6.impl.update.UpdatePolicyImpl
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.Executor

class BillingLibraryMonitor(
    private val context: Context,
    private val workerExecutor: Executor,
    private val uiExecutor: Executor,
    private val billingInfoStorage: BillingInfoStorage,
    private val billingInfoSender: BillingInfoSender,
    private val billingInfoManager: BillingInfoManager = BillingInfoManagerImpl(billingInfoStorage),
    private val updatePolicy: UpdatePolicy = UpdatePolicyImpl()
) : BillingMonitor {

    private var billingConfig: BillingConfig? = null

    @WorkerThread
    override fun onSessionResumed() {
        DebugLogger.info(MODULE_TAG, "onSessionResumed with billingConfig=$billingConfig")
        updateBilling(billingConfig)
    }

    @Synchronized
    override fun onBillingConfigChanged(billingConfig: BillingConfig?) {
        DebugLogger.info(MODULE_TAG, "onBillingConfigChanged: $billingConfig")
        if (this.billingConfig == billingConfig) {
            return
        }
        this.billingConfig = billingConfig
        updateBilling(billingConfig)
    }

    private fun updateBilling(billingConfig: BillingConfig?) {
        DebugLogger.info(MODULE_TAG, "updateBilling with billingConfig=$billingConfig")
        billingConfig ?: return
        uiExecutor.execute(object : SafeRunnable() {
            override fun runSafety() {
                val billingClient = BillingClient
                    .newBuilder(context)
                    .setListener(PurchasesUpdatedListenerImpl())
                    .enablePendingPurchases()
                    .build()
                billingClient.startConnection(
                    BillingClientStateListenerImpl(
                        billingConfig,
                        billingClient,
                        object : UtilsProvider {
                            override fun getBillingInfoManager() = this@BillingLibraryMonitor.billingInfoManager

                            override fun getUpdatePolicy() = this@BillingLibraryMonitor.updatePolicy

                            override fun getBillingInfoSender() = this@BillingLibraryMonitor.billingInfoSender

                            override fun getUiExecutor() = this@BillingLibraryMonitor.uiExecutor

                            override fun getWorkerExecutor() = this@BillingLibraryMonitor.workerExecutor
                        }
                    )
                )
            }
        })
    }
}
