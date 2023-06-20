package io.appmetrica.analytics.billingv4.internal

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
import io.appmetrica.analytics.billingv4.impl.library.BillingClientStateListenerImpl
import io.appmetrica.analytics.billingv4.impl.library.PurchasesUpdatedListenerImpl
import io.appmetrica.analytics.billingv4.impl.storage.BillingInfoManagerImpl
import io.appmetrica.analytics.billingv4.impl.update.UpdatePolicyImpl
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import java.util.concurrent.Executor

private const val TAG = "[BillingLibraryMonitor]"

class BillingLibraryMonitor(
    private val context: Context,
    private val workerExecutor: Executor,
    private val uiExecutor: Executor,
    private val billingInfoStorage: BillingInfoStorage,
    private val billingInfoSender: BillingInfoSender,
    private val billingInfoManager: BillingInfoManager = BillingInfoManagerImpl(billingInfoStorage),
    private val updatePolicy: UpdatePolicy = UpdatePolicyImpl()
) : BillingMonitor, UtilsProvider {

    private var billingConfig: BillingConfig? = null

    @WorkerThread
    override fun onSessionResumed() {
        YLogger.info(TAG, "onSessionResumed with billingConfig=$billingConfig")
        val localBillingConfig = billingConfig ?: return
        uiExecutor.execute(object : SafeRunnable() {
            override fun runSafety() {
                val billingClient = BillingClient
                    .newBuilder(context)
                    .setListener(PurchasesUpdatedListenerImpl())
                    .enablePendingPurchases()
                    .build()
                billingClient.startConnection(
                    BillingClientStateListenerImpl(
                        localBillingConfig,
                        billingClient,
                        this@BillingLibraryMonitor
                    )
                )
            }
        })
    }

    @Synchronized
    override fun onBillingConfigChanged(billingConfig: BillingConfig?) {
        YLogger.info(TAG, "onBillingConfigChanged: $billingConfig")
        this.billingConfig = billingConfig
    }

    override fun getBillingInfoManager() = billingInfoManager

    override fun getUpdatePolicy() = updatePolicy

    override fun getBillingInfoSender() = billingInfoSender

    override fun getUiExecutor() = uiExecutor

    override fun getWorkerExecutor() = workerExecutor
}
