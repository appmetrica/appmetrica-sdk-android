package io.appmetrica.analytics.productflow.impl.events.eventdata

import io.appmetrica.analytics.coreutils.internal.StringUtils.getUTF8Bytes
import io.appmetrica.analytics.coreutils.internal.proto.DecimalProtoModel
import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants
import io.appmetrica.analytics.productflow.impl.events.model.FlowStepEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class FlowStepEventDataTest : CommonTest() {

    @Test
    fun `serializes flow step event to protobuf`() {
        val event = FlowStepEvent(
            productId = "product-1",
            productOfferId = null,
            stepType = "documents",
            stepOption = "passport",
            price = null,
            payload = null,
        )
        val eventData = FlowStepEventData(event)

        assertThat(eventData.type).isEqualTo(ProductFlowConstants.CUSTOM_EVENT_TYPE)
        assertThat(eventData.bytesTruncated).isZero()
        assertThat(eventData.description).contains("documents", "passport", "product-1")

        val proto = ProductFlowEventProto.parseFrom(eventData.data)
        ProtoObjectPropertyAssertions(proto)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_STEP)
            .checkField("productId", getUTF8Bytes("product-1"))
            .checkField("productOfferId", getUTF8Bytes(null as String?))
            .checkFieldIsNull("price")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("shownInfo")
            .checkFieldIsNull("resultInfo")
            .checkFieldRecursively(
                "stepInfo"
            ) { stepInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.StepInfo> ->
                stepInfoAssertions
                    .checkField("stepType", getUTF8Bytes("documents"))
                    .checkField("stepOption", getUTF8Bytes("passport"))
            }
            .checkAll()
    }

    @Test
    fun `serializes flow step event with price and payload`() {
        val price = OfferPrice(100, "USD")
        val event = FlowStepEvent(
            productId = "product-1",
            productOfferId = "offer-1",
            stepType = "payment",
            stepOption = null,
            price = price,
            payload = mapOf("method" to "card"),
        )
        val eventData = FlowStepEventData(event)

        val proto = ProductFlowEventProto.parseFrom(eventData.data)
        ProtoObjectPropertyAssertions(proto)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_STEP)
            .checkField("productId", getUTF8Bytes("product-1"))
            .checkField("productOfferId", getUTF8Bytes("offer-1"))
            .checkFieldIsNull("shownInfo")
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
                                key = getUTF8Bytes("method")
                                value = getUTF8Bytes("card")
                            },
                        ),
                    )
            }
            .checkFieldRecursively(
                "stepInfo"
            ) { stepInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.StepInfo> ->
                stepInfoAssertions
                    .checkField("stepType", getUTF8Bytes("payment"))
                    .checkField("stepOption", getUTF8Bytes(null as String?))
            }
            .checkAll()
    }

    @Test
    fun `truncates cyrillic step type by character limit and reports bytes truncated`() {
        val fittingCyrillic = "ы".repeat(ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE)
        val event = FlowStepEvent(
            productId = "product-1",
            productOfferId = null,
            stepType = fittingCyrillic + "кошка",
            stepOption = null,
            price = null,
            payload = null,
        )
        val eventData = FlowStepEventData(event)

        assertThat(eventData.bytesTruncated).isEqualTo(10)

        val proto = ProductFlowEventProto.parseFrom(eventData.data)
        ProtoObjectPropertyAssertions(proto)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_STEP)
            .checkField("productId", getUTF8Bytes("product-1"))
            .checkField("productOfferId", getUTF8Bytes(null as String?))
            .checkFieldIsNull("price")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("shownInfo")
            .checkFieldIsNull("resultInfo")
            .checkFieldRecursively(
                "stepInfo"
            ) { stepInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.StepInfo> ->
                stepInfoAssertions
                    .checkField("stepType", getUTF8Bytes(fittingCyrillic))
                    .checkField("stepOption", getUTF8Bytes(null as String?))
            }
            .checkAll()
    }
}
