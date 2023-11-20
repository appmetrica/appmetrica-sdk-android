package io.appmetrica.analytics.billingv6.impl.library

import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.PurchasesResponseListener
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billingv6.impl.BillingUtils
import io.appmetrica.analytics.billingv6.impl.ProductInfoCreator
import io.appmetrica.analytics.billingv6.impl.TAG
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

internal class PurchaseResponseListenerImpl(
    private val type: String,
    private val utilsProvider: UtilsProvider,
    private val billingInfoSentListener: () -> Unit,
    private val purchaseHistoryRecords: List<PurchaseHistoryRecord>,
    private val productDetails: List<ProductDetails>,
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder
) : PurchasesResponseListener {

    override fun onQueryPurchasesResponse(
        billingResult: BillingResult,
        purchases: List<Purchase>
    ) {
        utilsProvider.workerExecutor.execute(object : SafeRunnable() {
            override fun runSafety() {
                processResponse(billingResult, purchases)
                billingLibraryConnectionHolder.removeListener(this@PurchaseResponseListenerImpl)
            }
        })
    }

    @WorkerThread
    private fun processResponse(
        billingResult: BillingResult,
        purchases: List<Purchase>
    ) {
        YLogger.info(
            TAG,
            "onQueryPurchasesResponse type=$type, " +
                "result=${BillingUtils.toString(billingResult)}, " +
                "list=$purchases"
        )
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            return
        }
        val purchasesMap = createPurchasesMap(purchases)
        val purchasesHistoryRecordsMap = createPurchasesHistoryRecordsMap(purchaseHistoryRecords)
        val productInfos = productDetails.mapNotNull { record ->
            purchasesHistoryRecordsMap[record.productId]?.let { purchasesHistoryRecord ->
                ProductInfoCreator.createFrom(purchasesHistoryRecord, record, purchasesMap[record.productId])
            }
        }
        YLogger.debug(TAG, "Product info to send $productInfos")
        utilsProvider.billingInfoSender.sendInfo(productInfos)
        billingInfoSentListener()
    }

    @WorkerThread
    private fun createPurchasesMap(
        purchases: List<Purchase>
    ): Map<String, Purchase> {
        return mutableMapOf<String, Purchase>().also {
            for (purchase in purchases) {
                for (productId in purchase.products) {
                    it[productId] = purchase
                }
            }
        }
    }

    @WorkerThread
    private fun createPurchasesHistoryRecordsMap(
        purchaseHistoryRecords: List<PurchaseHistoryRecord>
    ): Map<String, PurchaseHistoryRecord> {
        return mutableMapOf<String, PurchaseHistoryRecord>().also {
            for (purchase in purchaseHistoryRecords) {
                for (productId in purchase.products) {
                    it[productId] = purchase
                }
            }
        }
    }
}
