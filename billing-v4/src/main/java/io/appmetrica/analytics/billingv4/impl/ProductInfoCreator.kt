package io.appmetrica.analytics.billingv4.impl

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.SkuDetails
import io.appmetrica.analytics.billinginterface.internal.Period
import io.appmetrica.analytics.billinginterface.internal.ProductInfo

object ProductInfoCreator {

    fun createFrom(
        purchasesHistoryRecord: PurchaseHistoryRecord,
        skuDetails: SkuDetails,
        purchase: Purchase?
    ): ProductInfo {
        return ProductInfo(
            ProductTypeParser.parse(skuDetails.type),
            skuDetails.sku,
            purchasesHistoryRecord.quantity,
            skuDetails.priceAmountMicros,
            skuDetails.priceCurrencyCode,
            getIntroductoryPriceAmountMicros(skuDetails),
            getIntroductoryPricePeriod(skuDetails),
            getIntroductoryPriceCycles(skuDetails),
            Period.parse(skuDetails.subscriptionPeriod),
            purchasesHistoryRecord.signature,
            purchasesHistoryRecord.purchaseToken,
            purchasesHistoryRecord.purchaseTime,
            purchase?.isAutoRenewing ?: false,
            purchase?.originalJson ?: "{}"
        )
    }

    private fun getIntroductoryPriceCycles(skuDetails: SkuDetails): Int {
        return if (skuDetails.freeTrialPeriod.isEmpty()) {
            skuDetails.introductoryPriceCycles
        } else {
            1
        }
    }

    private fun getIntroductoryPriceAmountMicros(skuDetails: SkuDetails): Long {
        return if (skuDetails.freeTrialPeriod.isEmpty()) {
            skuDetails.introductoryPriceAmountMicros
        } else {
            0
        }
    }

    private fun getIntroductoryPricePeriod(skuDetails: SkuDetails): Period? {
        return if (skuDetails.freeTrialPeriod.isEmpty()) {
            Period.parse(skuDetails.introductoryPricePeriod)
        } else {
            Period.parse(skuDetails.freeTrialPeriod)
        }
    }
}
