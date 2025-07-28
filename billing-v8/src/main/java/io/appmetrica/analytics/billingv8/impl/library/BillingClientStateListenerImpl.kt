package io.appmetrica.analytics.billingv8.impl.library

import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.QueryPurchasesParams
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billingv8.impl.BillingUtils
import io.appmetrica.analytics.billingv8.impl.MODULE_TAG
import io.appmetrica.analytics.billingv8.impl.UpdateBillingProgressCallback
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class BillingClientStateListenerImpl @VisibleForTesting constructor(
    private val config: BillingConfig,
    private val billingClient: BillingClient,
    private val utilsProvider: UtilsProvider,
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder,
    private val updateBillingProgressCallback: UpdateBillingProgressCallback,
) : BillingClientStateListener {

    constructor(
        config: BillingConfig,
        billingClient: BillingClient,
        callback: UpdateBillingProgressCallback,
        utilsProvider: UtilsProvider,
    ) : this(
        config,
        billingClient,
        utilsProvider,
        BillingLibraryConnectionHolder(
            billingClient
        ),
        callback
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
        DebugLogger.info(MODULE_TAG, "onBillingSetupFinished result=${BillingUtils.toString(billingResult)}")
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            updateBillingProgressCallback.onUpdateFinished()
            return
        }
        if (!billingClient.isReady) {
            updateBillingProgressCallback.onUpdateFinished()
            return
        }
        listOf(BillingClient.ProductType.INAPP, BillingClient.ProductType.SUBS).forEach { type ->
            val listener = PurchaseHistoryResponseListenerImpl(
                config,
                billingClient,
                utilsProvider,
                type,
                billingLibraryConnectionHolder,
                updateBillingProgressCallback
            )
            billingLibraryConnectionHolder.addListener(listener)
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(type)
                    .build(),
                listener
            )
        }
    }

    @UiThread
    override fun onBillingServiceDisconnected() {
        DebugLogger.info(MODULE_TAG, "onBillingServiceDisconnected")
    }
}
