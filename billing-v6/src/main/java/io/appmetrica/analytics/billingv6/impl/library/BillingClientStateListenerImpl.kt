package io.appmetrica.analytics.billingv6.impl.library

import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.QueryPurchaseHistoryParams
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billingv6.impl.BillingUtils
import io.appmetrica.analytics.billingv6.impl.TAG
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.logger.internal.YLogger

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
    ) : this(
        config,
        billingClient,
        utilsProvider,
        BillingLibraryConnectionHolder(
            billingClient,
            utilsProvider
        )
    )

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
        listOf(BillingClient.ProductType.INAPP, BillingClient.ProductType.SUBS).forEach { type ->
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
                        billingClient.queryPurchaseHistoryAsync(
                            QueryPurchaseHistoryParams.newBuilder()
                                .setProductType(type)
                                .build(),
                            listener
                        )
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
