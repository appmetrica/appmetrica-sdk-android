package io.appmetrica.analytics.billingv6.impl.library

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.PurchaseHistoryResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billingv6.impl.BillingUtils
import io.appmetrica.analytics.billingv6.impl.MODULE_TAG
import io.appmetrica.analytics.billingv6.impl.ProductTypeParser
import io.appmetrica.analytics.billingv6.impl.UpdateBillingProgressCallback
import io.appmetrica.analytics.billingv6.impl.storage.StorageUpdater
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class PurchaseHistoryResponseListenerImpl(
    private val config: BillingConfig,
    private val billingClient: BillingClient,
    private val utilsProvider: UtilsProvider,
    private val type: String,
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder,
    private val updateBillingProgressCallback: UpdateBillingProgressCallback,
) : PurchaseHistoryResponseListener {

    @UiThread
    override fun onPurchaseHistoryResponse(
        billingResult: BillingResult,
        list: List<PurchaseHistoryRecord>?
    ) {
        utilsProvider.workerExecutor.execute(object : SafeRunnable() {
            override fun runSafety() {
                processResponse(billingResult, list)
                billingLibraryConnectionHolder.removeListener(this@PurchaseHistoryResponseListenerImpl)
            }
        })
    }

    @WorkerThread
    private fun processResponse(
        billingResult: BillingResult,
        purchaseHistoryRecords: List<PurchaseHistoryRecord>?
    ) {
        DebugLogger.info(
            MODULE_TAG,
            "onPurchaseHistoryResponse type=$type, " +
                "result=${BillingUtils.toString(billingResult)}, " +
                "list=$purchaseHistoryRecords"
        )
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK || purchaseHistoryRecords == null) {
            updateBillingProgressCallback.onUpdateFinished()
            return
        }
        val history = extractBillingInfo(purchaseHistoryRecords)
        val newBillingInfo = utilsProvider.updatePolicy.getBillingInfoToUpdate(
            config, history, utilsProvider.billingInfoManager
        )
        if (newBillingInfo.isEmpty()) {
            StorageUpdater.updateStorage(history, newBillingInfo, type, utilsProvider.billingInfoManager)
            updateBillingProgressCallback.onUpdateFinished()
        } else {
            querySkuDetails(
                purchaseHistoryRecords,
                newBillingInfo.keys.toList(),
                updateBillingProgressCallback
            ) {
                StorageUpdater.updateStorage(history, newBillingInfo, type, utilsProvider.billingInfoManager)
            }
        }
    }

    @WorkerThread
    private fun extractBillingInfo(purchaseHistoryRecords: List<PurchaseHistoryRecord>): Map<String, BillingInfo> {
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
        purchaseHistoryRecords: List<PurchaseHistoryRecord>,
        newSkus: List<String>,
        updateBillingProgressCallback: UpdateBillingProgressCallback,
        billingInfoSentListener: () -> Unit
    ) {
        val listener = ProductDetailsResponseListenerImpl(
            type,
            billingClient,
            utilsProvider,
            billingInfoSentListener,
            purchaseHistoryRecords,
            billingLibraryConnectionHolder,
            updateBillingProgressCallback
        )
        billingLibraryConnectionHolder.addListener(listener)
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
            billingLibraryConnectionHolder.removeListener(listener)
            updateBillingProgressCallback.onUpdateFinished()
        }
    }
}
