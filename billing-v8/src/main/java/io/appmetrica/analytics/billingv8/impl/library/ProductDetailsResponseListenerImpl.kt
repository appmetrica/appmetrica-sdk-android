package io.appmetrica.analytics.billingv8.impl.library

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider
import io.appmetrica.analytics.billingv8.impl.BillingUtils
import io.appmetrica.analytics.billingv8.impl.Constants.MODULE_TAG
import io.appmetrica.analytics.billingv8.impl.UpdateBillingProgressCallback
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ProductDetailsResponseListenerImpl(
    private val type: String,
    private val billingClient: BillingClient,
    private val utilsProvider: UtilsProvider,
    private val billingInfoSentListener: () -> Unit,
    private val purchaseHistoryRecords: List<Purchase>,
    private val billingLibraryConnectionHolder: BillingLibraryConnectionHolder,
    private val updateBillingProgressCallback: UpdateBillingProgressCallback,
) : ProductDetailsResponseListener {

    @UiThread
    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetails: QueryProductDetailsResult
    ) {
        utilsProvider.workerExecutor.execute(object : SafeRunnable() {
            override fun runSafety() {
                processResponse(billingResult, productDetails.productDetailsList)
                billingLibraryConnectionHolder.removeListener(this@ProductDetailsResponseListenerImpl)
            }
        })
    }

    @WorkerThread
    private fun processResponse(
        billingResult: BillingResult,
        productDetails: List<ProductDetails>
    ) {
        DebugLogger.info(
            MODULE_TAG,
            "onSkuDetailsResponse type=$type, " +
                "result=${BillingUtils.toString(billingResult)}, " +
                "list=$productDetails"
        )
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK || productDetails.isEmpty()) {
            updateBillingProgressCallback.onUpdateFinished()
            return
        }
        val listener = PurchaseResponseListenerImpl(
            type,
            utilsProvider,
            billingInfoSentListener,
            purchaseHistoryRecords,
            productDetails,
            billingLibraryConnectionHolder,
            updateBillingProgressCallback
        )
        billingLibraryConnectionHolder.addListener(listener)
        if (billingClient.isReady) {
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(type)
                    .build(),
                listener
            )
        } else {
            billingLibraryConnectionHolder.removeListener(listener)
            updateBillingProgressCallback.onUpdateFinished()
        }
    }
}
