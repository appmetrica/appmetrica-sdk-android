package io.appmetrica.analytics.billing.impl.storage

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.billing.impl.protobuf.client.AutoInappCollectingInfoProto
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BillingInfoConverterTest : CommonTest() {

    private var converter = BillingInfoConverter()

    @Test
    fun toProtoIfInapp() {
        val model = BillingInfo(ProductType.INAPP, "sku", "purchaseToken", 41, 42)
        val proto = converter.fromModel(model)

        ProtoObjectPropertyAssertions(proto)
            .checkField("type", AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE)
            .checkField("sku", "sku")
            .checkField("purchaseToken", "purchaseToken")
            .checkField("purchaseTime", 41L)
            .checkField("sendTime", 42L)
            .checkAll()
    }

    @Test
    fun toProtoIfSubs() {
        val model = BillingInfo(ProductType.SUBS, "sku", "purchaseToken", 41, 42)
        val proto = converter.fromModel(model)

        ProtoObjectPropertyAssertions(proto)
            .checkField("type", AutoInappCollectingInfoProto.AutoInappCollectingInfo.SUBSCRIPTION)
            .checkField("sku", "sku")
            .checkField("purchaseToken", "purchaseToken")
            .checkField("purchaseTime", 41L)
            .checkField("sendTime", 42L)
            .checkAll()
    }

    @Test
    fun toProtoIfUnknown() {
        val model = BillingInfo(ProductType.UNKNOWN, "sku", "purchaseToken", 41, 42)
        val proto = converter.fromModel(model)

        ProtoObjectPropertyAssertions(proto)
            .checkField("type", AutoInappCollectingInfoProto.AutoInappCollectingInfo.UNKNOWN)
            .checkField("sku", "sku")
            .checkField("purchaseToken", "purchaseToken")
            .checkField("purchaseTime", 41L)
            .checkField("sendTime", 42L)
            .checkAll()
    }

    @Test
    fun toModelEmpty() {
        val model = converter.toModel(AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo())
        ObjectPropertyAssertions(model)
            .withFinalFieldOnly(false)
            .checkField("type", ProductType.UNKNOWN)
            .checkField("productId", "")
            .checkField("purchaseToken", "")
            .checkField("purchaseTime", 0L)
            .checkField("sendTime", 0L)
            .checkAll()
    }

    @Test
    @Throws(Exception::class)
    fun toModelIfPurchase() {
        val nano = AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo().apply {
            type = AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE
            sku = "sku"
            purchaseToken = "purchaseToken"
            purchaseTime = 41
            sendTime = 42
        }

        ObjectPropertyAssertions(converter.toModel(nano))
            .withFinalFieldOnly(false)
            .checkField("type", ProductType.INAPP)
            .checkField("productId", "sku")
            .checkField("purchaseToken", "purchaseToken")
            .checkField("purchaseTime", 41L)
            .checkField("sendTime", 42L)
            .checkAll()
    }

    @Test
    fun toModelIfSubscription() {
        val nano = AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo().apply {
            type = AutoInappCollectingInfoProto.AutoInappCollectingInfo.SUBSCRIPTION
            sku = "sku"
            purchaseToken = "purchaseToken"
            purchaseTime = 41
            sendTime = 42
        }

        ObjectPropertyAssertions(converter.toModel(nano))
            .withFinalFieldOnly(false)
            .checkField("type", ProductType.SUBS)
            .checkField("productId", "sku")
            .checkField("purchaseToken", "purchaseToken")
            .checkField("purchaseTime", 41L)
            .checkField("sendTime", 42L)
            .checkAll()
    }

    @Test
    fun toModelIfUnknown() {
        val nano = AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo().apply {
            type = AutoInappCollectingInfoProto.AutoInappCollectingInfo.UNKNOWN
            sku = "sku"
            purchaseToken = "purchaseToken"
            purchaseTime = 41
            sendTime = 42
        }

        ObjectPropertyAssertions(converter.toModel(nano))
            .withFinalFieldOnly(false)
            .checkField("type", ProductType.UNKNOWN)
            .checkField("productId", "sku")
            .checkField("purchaseToken", "purchaseToken")
            .checkField("purchaseTime", 41L)
            .checkField("sendTime", 42L)
            .checkAll()
    }

    @Test
    fun toModelIfOtherType() {
        val nano = AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo().apply {
            type = 42
            sku = "sku"
            purchaseToken = "purchaseToken"
            purchaseTime = 41
            sendTime = 42
        }

        ObjectPropertyAssertions(converter.toModel(nano))
            .withFinalFieldOnly(false)
            .checkField("type", ProductType.UNKNOWN)
            .checkField("productId", "sku")
            .checkField("purchaseToken", "purchaseToken")
            .checkField("purchaseTime", 41L)
            .checkField("sendTime", 42L)
            .checkAll()
    }
}
