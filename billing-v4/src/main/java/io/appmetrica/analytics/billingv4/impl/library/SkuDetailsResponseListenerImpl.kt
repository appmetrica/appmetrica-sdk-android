package io.appmetrica.analytics.billingv4.impl.library

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsResponseListener
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billingv4.impl.BillingUtils
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

private const val TAG = "[SkuDetailsResponseListenerImpl]"

internal class SkuDetailsResponseListenerImpl(
    private val type: String,
    private val billingClient: BillingClient,
    private val utilsProvider: UtilsProvider,
    private val billingInfoSentListener: () -> Unit,
    private val purchaseHistoryRecords: List<PurchaseHistoryRecord>,
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder
) : SkuDetailsResponseListener {

    @UiThread
    override fun onSkuDetailsResponse(
        billingResult: BillingResult,
        skuDetails: List<SkuDetails>?
    ) {
        utilsProvider.workerExecutor.execute(object : SafeRunnable() {
            override fun runSafety() {
                processResponse(billingResult, skuDetails)
                billingLibraryConnectionHolder.removeListener(this@SkuDetailsResponseListenerImpl)
            }
        })
    }

    @WorkerThread
    private fun processResponse(
        billingResult: BillingResult,
        skuDetails: List<SkuDetails>?
    ) {
        YLogger.info(
            TAG,
            "onSkuDetailsResponse type=$type, " +
                "result=${BillingUtils.toString(billingResult)}, " +
                "list=$skuDetails"
        )
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK || skuDetails.isNullOrEmpty()) {
            return
        }
        val listener = PurchaseResponseListenerImpl(
            type,
            utilsProvider,
            billingInfoSentListener,
            purchaseHistoryRecords,
            skuDetails,
            billingLibraryConnectionHolder
        )
        billingLibraryConnectionHolder.addListener(listener)
        utilsProvider.uiExecutor.execute(object : SafeRunnable() {
            override fun runSafety() {
                if (billingClient.isReady) {
                    billingClient.queryPurchasesAsync(type, listener)
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
