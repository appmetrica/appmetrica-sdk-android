package io.appmetrica.analytics.billingv8.impl.library

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billingv8.impl.BillingUtils
import io.appmetrica.analytics.billingv8.impl.Constants.MODULE_TAG
import io.appmetrica.analytics.billingv8.impl.ProductTypeParser
import io.appmetrica.analytics.billingv8.impl.storage.StorageUpdater
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class PurchaseHistoryResponseListenerImpl(
    private val config: BillingConfig,
    private val billingClient: BillingClient,
    private val utilsProvider: UtilsProvider,
    private val type: String,
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder,
) : PurchasesResponseListener {

    @UiThread
    override fun onQueryPurchasesResponse(
        billingResult: BillingResult,
        list: List<Purchase>
    ) {
        utilsProvider.workerExecutor.post(object : SafeRunnable() {
            override fun runSafety() {
                if (!billingLibraryConnectionHolder.isWaitingFor(this@PurchaseHistoryResponseListenerImpl)) return
                try {
                    processResponse(billingResult, list)
                } catch (e: Throwable) {
                    billingLibraryConnectionHolder.markFailed()
                    throw e
                } finally {
                    billingLibraryConnectionHolder.removeListener(this@PurchaseHistoryResponseListenerImpl)
                }
            }
        })
    }

    @WorkerThread
    private fun processResponse(
        billingResult: BillingResult,
        purchaseHistoryRecords: List<Purchase>
    ) {
        DebugLogger.info(
            MODULE_TAG,
            "onPurchaseHistoryResponse type=$type, " +
                "result=${BillingUtils.toString(billingResult)}, " +
                "list=$purchaseHistoryRecords"
        )
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            billingLibraryConnectionHolder.markFailed()
            return
        }
        val history = extractBillingInfo(purchaseHistoryRecords)
        val newBillingInfo = utilsProvider.updatePolicy.getBillingInfoToUpdate(
            config, history, utilsProvider.billingInfoManager
        )
        if (newBillingInfo.isEmpty()) {
            StorageUpdater.updateStorage(history, newBillingInfo, type, utilsProvider.billingInfoManager)
        } else {
            querySkuDetails(
                purchaseHistoryRecords,
                newBillingInfo.keys.toList(),
            ) {
                StorageUpdater.updateStorage(history, newBillingInfo, type, utilsProvider.billingInfoManager)
            }
        }
    }

    @WorkerThread
    private fun extractBillingInfo(purchaseHistoryRecords: List<Purchase>): Map<String, BillingInfo> {
        val result = mutableMapOf<String, BillingInfo>()
        for (record in purchaseHistoryRecords) {
            for (productId in record.products) {
                val info = BillingInfo(
                    ProductTypeParser.parse(type),
                    productId,
                    record.purchaseToken,
                    record.purchaseTime,
                    0
                )
                result[info.productId] = info
            }
        }
        DebugLogger.info(MODULE_TAG, "Billing info from history $result")
        return result
    }

    @WorkerThread
    private fun querySkuDetails(
        purchaseHistoryRecords: List<Purchase>,
        newSkus: List<String>,
        billingInfoSentListener: () -> Unit
    ) {
        val listener = ProductDetailsResponseListenerImpl(
            type,
            billingClient,
            utilsProvider,
            billingInfoSentListener,
            purchaseHistoryRecords,
            billingLibraryConnectionHolder
        )
        billingLibraryConnectionHolder.addListener(listener)
        billingLibraryConnectionHolder.startRequest(listener) {
            if (billingClient.isReady) {
                billingClient.queryProductDetailsAsync(
                    QueryProductDetailsParams.newBuilder()
                        .setProductList(
                            newSkus.map { productId ->
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(productId)
                                    .setProductType(type)
                                    .build()
                            }
                        )
                        .build(),
                    listener
                )
            } else {
                billingLibraryConnectionHolder.markFailed()
                billingLibraryConnectionHolder.removeListener(listener)
            }
        }
    }
}
