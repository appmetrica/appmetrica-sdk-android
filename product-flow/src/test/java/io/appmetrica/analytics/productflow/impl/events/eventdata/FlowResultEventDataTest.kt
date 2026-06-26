package io.appmetrica.analytics.productflow.impl.events.eventdata

import io.appmetrica.analytics.coreutils.internal.StringUtils.getUTF8Bytes
import io.appmetrica.analytics.coreutils.internal.proto.DecimalProtoModel
import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.analytics.productflow.ProductFlowStatus
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants
import io.appmetrica.analytics.productflow.impl.events.model.FlowResultEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class FlowResultEventDataTest : CommonTest() {

    @Test
    fun `serializes flow result event to protobuf`() {
        val price = OfferPrice(BigDecimal("50"), "USD")
        val payload = mapOf("reason" to "declined")
        val event = FlowResultEvent(
            status = ProductFlowStatus.DECLINED,
            productId = null,
            productOfferId = "offer-1",
            price = price,
            payload = payload,
        )
        val eventData = FlowResultEventData(event)

        assertThat(eventData.type).isEqualTo(ProductFlowConstants.CUSTOM_EVENT_TYPE)
        assertThat(eventData.bytesTruncated).isZero()
        assertThat(eventData.description).contains("DECLINED", "offer-1", "reason")

        val proto = ProductFlowEventProto.parseFrom(eventData.data)
        ProtoObjectPropertyAssertions(proto)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_FLOW_RESULT)
            .checkField("productId", getUTF8Bytes(null as String?))
            .checkField("productOfferId", getUTF8Bytes("offer-1"))
            .checkFieldIsNull("shownInfo")
            .checkFieldIsNull("stepInfo")
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
                                key = getUTF8Bytes("reason")
                                value = getUTF8Bytes("declined")
                            },
                        ),
                    )
            }
            .checkFieldRecursively(
                "resultInfo"
            ) { resultInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.ResultInfo> ->
                resultInfoAssertions
                    .checkField("status", ProductFlowEventProto.ResultInfo.STATUS_DECLINED)
            }
            .checkAll()
    }

    @Test
    fun `truncates cyrillic product offer id by character limit and reports bytes truncated`() {
        val fittingCyrillic = "ы".repeat(ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE)
        val event = FlowResultEvent(
            status = ProductFlowStatus.SUCCESS,
            productId = null,
            productOfferId = fittingCyrillic + "кошка",
            price = null,
            payload = null,
        )
        val eventData = FlowResultEventData(event)

        assertThat(eventData.bytesTruncated).isEqualTo(10)

        val proto = ProductFlowEventProto.parseFrom(eventData.data)
        ProtoObjectPropertyAssertions(proto)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_FLOW_RESULT)
            .checkField("productId", getUTF8Bytes(null as String?))
            .checkField("productOfferId", getUTF8Bytes(fittingCyrillic))
            .checkFieldIsNull("shownInfo")
            .checkFieldIsNull("stepInfo")
            .checkFieldIsNull("price")
            .checkFieldIsNull("payload")
            .checkFieldRecursively(
                "resultInfo"
            ) { resultInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.ResultInfo> ->
                resultInfoAssertions
                    .checkField("status", ProductFlowEventProto.ResultInfo.STATUS_SUCCESS)
            }
            .checkAll()
    }
}
