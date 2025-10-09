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
class AutoInappCollectingInfoConverterTest : CommonTest() {

    private val billingInfoList = listOf(
        BillingInfo(ProductType.INAPP, "sku", "purchaseToken", 41, 42)
    )
    private val billingInfoListProto = arrayOf(
        AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo().apply {
            type = AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE
            sku = "sku"
            purchaseToken = "purchaseToken"
            purchaseTime = 41L
            sendTime = 42L
        }
    )

    private val converter = AutoInappCollectingInfoConverter()

    @Test
    fun defaultToProto() {
        val autoInappCollectingInfo = AutoInappCollectingInfo(emptyList(), false)

        ProtoObjectPropertyAssertions(converter.fromModel(autoInappCollectingInfo))
            .checkField("firstInappCheckOccurred", false)
            .checkField(
                "entries",
                emptyArray<AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo>()
            )
            .checkAll()
    }

    @Test
    fun filledToProto() {
        val autoInappCollectingInfo = AutoInappCollectingInfo(billingInfoList, true)

        ProtoObjectPropertyAssertions(converter.fromModel(autoInappCollectingInfo))
            .checkField("firstInappCheckOccurred", true)
            .checkFieldComparingFieldByFieldRecursively("entries", billingInfoListProto)
            .checkAll()
    }

    @Test
    fun defaultToModel() {
        val protoAutoInappCollectingInfo = AutoInappCollectingInfoProto.AutoInappCollectingInfo()

        ObjectPropertyAssertions(converter.toModel(protoAutoInappCollectingInfo))
            .checkField("firstInappCheckOccurred", false)
            .checkField("billingInfos", emptyList<BillingInfo>())
            .checkAll()
    }

    @Test
    fun filledToModel() {
        val protoAutoInappCollectingInfo = AutoInappCollectingInfoProto.AutoInappCollectingInfo().apply {
            firstInappCheckOccurred = true
            entries = billingInfoListProto
        }

        ObjectPropertyAssertions(converter.toModel(protoAutoInappCollectingInfo))
            .checkField("firstInappCheckOccurred", true)
            .checkFieldComparingFieldByFieldRecursively("billingInfos", billingInfoList)
            .checkAll()
    }
}
