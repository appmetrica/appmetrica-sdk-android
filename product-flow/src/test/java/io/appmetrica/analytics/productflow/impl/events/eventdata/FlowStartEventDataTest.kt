package io.appmetrica.analytics.productflow.impl.events.eventdata

import io.appmetrica.analytics.coreutils.internal.StringUtils.getUTF8Bytes
import io.appmetrica.analytics.coreutils.internal.proto.DecimalProtoModel
import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants
import io.appmetrica.analytics.productflow.impl.events.model.FlowStartEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class FlowStartEventDataTest : CommonTest() {

    @Test
    fun `serializes flow start event to protobuf`() {
        val price = OfferPrice(BigDecimal.TEN, "USD")
        val event = FlowStartEvent(
            productId = "product-1",
            productOfferId = "offer-1",
            price = price,
            payload = mapOf("source" to "banner"),
        )
        val eventData = FlowStartEventData(event)

        assertThat(eventData.type).isEqualTo(ProductFlowConstants.CUSTOM_EVENT_TYPE)
        assertThat(eventData.bytesTruncated).isZero()
        assertThat(eventData.description).contains("product-1", "offer-1")

        val proto = ProductFlowEventProto.parseFrom(eventData.data)
        ProtoObjectPropertyAssertions(proto)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_FLOW_START)
            .checkField("productId", getUTF8Bytes("product-1"))
            .checkField("productOfferId", getUTF8Bytes("offer-1"))
            .checkFieldIsNull("shownInfo")
            .checkFieldIsNull("stepInfo")
            .checkFieldIsNull("resultInfo")
            .checkFieldRecursively(
                "price"
            ) { priceAssertions: ObjectPropertyAssertions<ProductFlowEventProto.Amount> ->
                val decimal = DecimalProtoModel.fromDecimal(price.amount)
                priceAssertions
                    .checkField("mantissa", decimal.mantissa)
                    .checkField("exponent", decimal.exponent)
                    .checkField("unit", getUTF8Bytes(price.unit))
            }
            .checkFieldRecursively(
                "payload"
            ) { payloadAssertions: ObjectPropertyAssertions<ProductFlowEventProto.Payload> ->
                payloadAssertions
                    .checkField("truncatedPairsCount", 0)
                    .checkField(
                        "pairs",
                        arrayOf(
                            ProductFlowEventProto.Payload.Pair().apply {
                                key = getUTF8Bytes("source")
                                value = getUTF8Bytes("banner")
                            },
                        ),
                    )
            }
            .checkAll()
    }

    @Test
    fun `truncates cyrillic product id by character limit and reports bytes truncated`() {
        val fittingCyrillic = "ы".repeat(ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE)
        val event = FlowStartEvent(
            productId = fittingCyrillic + "кошка",
            productOfferId = null,
            price = null,
            payload = null,
        )
        val eventData = FlowStartEventData(event)

        assertThat(eventData.bytesTruncated).isEqualTo(10)

        val proto = ProductFlowEventProto.parseFrom(eventData.data)
        ProtoObjectPropertyAssertions(proto)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_FLOW_START)
            .checkField("productId", getUTF8Bytes(fittingCyrillic))
            .checkField("productOfferId", getUTF8Bytes(null as String?))
            .checkFieldIsNull("shownInfo")
            .checkFieldIsNull("stepInfo")
            .checkFieldIsNull("resultInfo")
            .checkFieldIsNull("price")
            .checkFieldIsNull("payload")
            .checkAll()
    }

    @Test
    fun `serializes minimal flow start event without optional fields`() {
        val event = FlowStartEvent(
            productId = "product-1",
            productOfferId = null,
            price = null,
            payload = null,
        )
        val eventData = FlowStartEventData(event)

        assertThat(eventData.bytesTruncated).isZero()

        val proto = ProductFlowEventProto.parseFrom(eventData.data)
        ProtoObjectPropertyAssertions(proto)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_FLOW_START)
            .checkField("productId", getUTF8Bytes("product-1"))
            .checkField("productOfferId", getUTF8Bytes(null as String?))
            .checkFieldIsNull("price")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("shownInfo")
            .checkFieldIsNull("stepInfo")
            .checkFieldIsNull("resultInfo")
            .checkAll()
    }
}
