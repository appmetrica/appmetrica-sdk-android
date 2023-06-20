package io.appmetrica.analytics.billingv4.impl.library

import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billingv4.impl.BillingUtils
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

private const val TAG = "[BillingClientStateListenerImpl]"

internal class BillingClientStateListenerImpl @VisibleForTesting constructor(
    private val config: BillingConfig,
    private val billingClient: BillingClient,
    private val utilsProvider: UtilsProvider,
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder
) : BillingClientStateListener {

    constructor(
        config: BillingConfig,
        billingClient: BillingClient,
        utilsProvider: UtilsProvider,
    ) : this(config, billingClient, utilsProvider, BillingLibraryConnectionHolder(billingClient))

    @UiThread
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        utilsProvider.workerExecutor.execute(object : SafeRunnable() {
            override fun runSafety() {
                processResult(billingResult)
            }
        })
    }

    @WorkerThread
    private fun processResult(billingResult: BillingResult) {
        YLogger.info(TAG, "onBillingSetupFinished result=${BillingUtils.toString(billingResult)}")
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            return
        }
        listOf(BillingClient.SkuType.INAPP, BillingClient.SkuType.SUBS).forEach { type ->
            val listener = PurchaseHistoryResponseListenerImpl(
                config,
                billingClient,
                utilsProvider,
                type,
                billingLibraryConnectionHolder
            )
            billingLibraryConnectionHolder.addListener(listener)
            utilsProvider.uiExecutor.execute(object : SafeRunnable() {
                override fun runSafety() {
                    if (billingClient.isReady) {
                        billingClient.queryPurchaseHistoryAsync(type, listener)
                    } else {
                        utilsProvider.workerExecutor.execute(object : SafeRunnable() {
                            override fun runSafety() {
                                billingLibraryConnectionHolder.removeListener(listener)
                            }
                        })
                    }
                }
            })
        }
    }

    @UiThread
    override fun onBillingServiceDisconnected() {
        YLogger.info(TAG, "onBillingServiceDisconnected")
    }
}
