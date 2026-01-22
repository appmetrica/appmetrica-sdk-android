package io.appmetrica.analytics.billing.impl.storage

import io.appmetrica.analytics.billing.impl.protobuf.client.AutoInappCollectingInfoProto
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AutoInappCollectingInfoSerializerTest : CommonTest() {
    private val serializer = AutoInappCollectingInfoSerializer()

    @Test
    fun toByteArrayDefaultObject() {
        val protoState = AutoInappCollectingInfoProto.AutoInappCollectingInfo()
        val rawData = serializer.toByteArray(protoState)
        val restored = serializer.toState(rawData)

        assertThat(restored).usingRecursiveComparison().isEqualTo(protoState)
    }

    @Test
    fun toByteArrayFilledObject() {
        val protoState = AutoInappCollectingInfoProto.AutoInappCollectingInfo().apply {
            entries = arrayOf(
                AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo().also {
                    it.type = AutoInappCollectingInfoProto.AutoInappCollectingInfo.PURCHASE
                    it.sku = "sku"
                    it.purchaseToken = "purchaseToken"
                    it.purchaseTime = 41
                    it.sendTime = 42
                }
            )
            firstInappCheckOccurred = false
        }

        val rawData = serializer.toByteArray(protoState)

        assertThat(rawData).isNotEmpty()

        val restored = serializer.toState(rawData)

        assertThat(restored).usingRecursiveComparison().isEqualTo(protoState)
    }

    @Test(expected = InvalidProtocolBufferNanoException::class)
    fun deserializationInvalidByteArray() {
        serializer.toState(byteArrayOf(1, 2, 3))
    }

    @Test
    fun defaultValue() {
        assertThat(serializer.defaultValue()).usingRecursiveComparison().isEqualTo(
            AutoInappCollectingInfoProto.AutoInappCollectingInfo()
        )
    }
}
