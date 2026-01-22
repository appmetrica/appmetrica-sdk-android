package io.appmetrica.analytics.billingv8.impl

import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import io.appmetrica.analytics.billinginterface.internal.ProductInfo
import io.appmetrica.analytics.billingv8.impl.Constants.MODULE_TAG
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal object ProductInfoCreator {

    fun createFrom(
        purchasesHistoryRecord: Purchase,
        skuDetails: ProductDetails,
        purchase: Purchase?
    ): ProductInfo? {
        return when (skuDetails.productType) {
            ProductType.INAPP -> createFromInapp(purchasesHistoryRecord, skuDetails, purchase)
            ProductType.SUBS -> createFromSubs(purchasesHistoryRecord, skuDetails, purchase)
            else -> {
                DebugLogger.info(
                    MODULE_TAG,
                    "createFrom unknown product type=${skuDetails.productType}, " +
                        "productId=${skuDetails.productId}"
                )
                null
            }
        }
    }

    private fun createFromSubs(
        purchasesHistoryRecord: Purchase,
        skuDetails: ProductDetails,
        purchase: Purchase?
    ): ProductInfo {
        return ProductInfo(
            ProductTypeParser.parse(skuDetails.productType),
            skuDetails.productId,
            purchasesHistoryRecord.quantity,
            0,
            "",
            0,
            null,
            1,
            null,
            purchasesHistoryRecord.signature,
            purchasesHistoryRecord.purchaseToken,
            purchasesHistoryRecord.purchaseTime,
            purchase?.isAutoRenewing ?: false,
            purchase?.originalJson ?: "{}"
        )
    }

    private fun createFromInapp(
        purchasesHistoryRecord: Purchase,
        skuDetails: ProductDetails,
        purchase: Purchase?
    ): ProductInfo {
        return ProductInfo(
            ProductTypeParser.parse(skuDetails.productType),
            skuDetails.productId,
            purchasesHistoryRecord.quantity,
            skuDetails.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0,
            skuDetails.oneTimePurchaseOfferDetails?.priceCurrencyCode ?: "",
            0,
            null,
            1,
            null,
            purchasesHistoryRecord.signature,
            purchasesHistoryRecord.purchaseToken,
            purchasesHistoryRecord.purchaseTime,
            purchase?.isAutoRenewing ?: false,
            purchase?.originalJson ?: "{}"
        )
    }
}
