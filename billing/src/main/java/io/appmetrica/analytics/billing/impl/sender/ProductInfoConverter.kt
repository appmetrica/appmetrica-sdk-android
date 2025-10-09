package io.appmetrica.analytics.billing.impl.sender

import io.appmetrica.analytics.billing.impl.protobuf.backend.Revenue
import io.appmetrica.analytics.billinginterface.internal.Period
import io.appmetrica.analytics.billinginterface.internal.ProductInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.protobuf.nano.MessageNano
import java.util.Currency
import java.util.concurrent.TimeUnit

class ProductInfoConverter {

    fun fromModel(value: ProductInfo): ByteArray = MessageNano.toByteArray(getRevenue(value))

    private fun getRevenue(productInfo: ProductInfo) = Revenue().apply {
        quantity = productInfo.quantity
        priceMicros = productInfo.priceMicros
        currency = getCurrency(productInfo.priceCurrency).toByteArray()
        productId = productInfo.sku.toByteArray()
        receipt = getReceipt(productInfo)
        autoCollected = true
        guessedBuyerDevice = Revenue.THIS
        inAppType = getInAppType(productInfo.type)
        transactionInfo = getTransactionInfo(productInfo)
        if (productInfo.type == ProductType.SUBS) {
            subscriptionInfo = getSubscriptionInfo(productInfo)
        }
    }

    private fun getCurrency(currency: String): String = try {
        Currency.getInstance(currency).currencyCode
    } catch (_: Throwable) {
        ""
    }

    private fun getReceipt(productInfo: ProductInfo) = Revenue.Receipt().apply {
        data = productInfo.purchaseOriginalJson.toByteArray()
        signature = productInfo.signature.toByteArray()
    }

    private fun getInAppType(type: ProductType): Int = when (type) {
        ProductType.INAPP -> Revenue.PURCHASE
        ProductType.SUBS -> Revenue.SUBSCRIPTION
        else -> Revenue.PURCHASE
    }

    private fun getTransactionInfo(productInfo: ProductInfo) = Revenue.Transaction().apply {
        id = productInfo.purchaseToken.toByteArray()
        time = TimeUnit.MILLISECONDS.toSeconds(productInfo.purchaseTime)
    }

    private fun getSubscriptionInfo(productInfo: ProductInfo) = Revenue.SubscriptionInfo().apply {
        autoRenewing = productInfo.autoRenewing
        productInfo.subscriptionPeriod?.let {
            subscriptionPeriod = toProtobufPeriod(it)
        }
        introductoryInfo = getIntroductory(productInfo)
    }

    private fun getIntroductory(productInfo: ProductInfo) = Revenue.SubscriptionInfo.Introductory().apply {
        priceMicros = productInfo.introductoryPriceMicros
        productInfo.introductoryPricePeriod?.let {
            period = toProtobufPeriod(it)
        }
        numberOfPeriods = productInfo.introductoryPriceCycles
    }

    private fun toProtobufPeriod(period: Period) = Revenue.SubscriptionInfo.Period().apply {
        number = period.number
        timeUnit = toTimeUnit(period.timeUnit)
    }

    private fun toTimeUnit(period: Period.TimeUnit): Int = when (period) {
        Period.TimeUnit.DAY -> Revenue.SubscriptionInfo.Period.DAY
        Period.TimeUnit.WEEK -> Revenue.SubscriptionInfo.Period.WEEK
        Period.TimeUnit.MONTH -> Revenue.SubscriptionInfo.Period.MONTH
        Period.TimeUnit.YEAR -> Revenue.SubscriptionInfo.Period.YEAR
        else -> Revenue.SubscriptionInfo.Period.TIME_UNIT_UNKNOWN
    }
}
