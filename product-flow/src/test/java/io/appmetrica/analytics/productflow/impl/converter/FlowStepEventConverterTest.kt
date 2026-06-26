package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils.getUTF8Bytes
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.analytics.productflow.impl.events.model.FlowStepEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class FlowStepEventConverterTest : CommonTest() {

    private val offerPriceConverter: OfferPriceConverter = mock()
    private val payloadConverter: PayloadConverter = mock()
    private val stringTrimmer: HierarchicalStringTrimmer = mock()
    private val converter = FlowStepEventConverter(
        offerPriceConverter,
        payloadConverter,
        stringTrimmer,
    )

    @Test
    fun `convert maps flow step event`() {
        val price = OfferPrice(BigDecimal.ONE, "USD")
        val payload = mapOf("step" to "details")
        val event = FlowStepEvent(
            productId = "product-1",
            productOfferId = "offer-1",
            stepType = "documents",
            stepOption = "passport",
            price = price,
            payload = payload,
        )

        whenever(stringTrimmer.trim("product-1")).thenReturn(TrimmingResult("product-1", BytesTruncatedInfo(1)))
        whenever(stringTrimmer.trim("offer-1")).thenReturn(TrimmingResult("offer-1", BytesTruncatedInfo(2)))
        whenever(stringTrimmer.trim("documents")).thenReturn(TrimmingResult("documents", BytesTruncatedInfo(3)))
        whenever(stringTrimmer.trim("passport")).thenReturn(TrimmingResult("passport", BytesTruncatedInfo(4)))

        val amountProto = ProductFlowEventProto.Amount()
        whenever(offerPriceConverter.convert(price)).thenReturn(
            TrimmingResult(amountProto, BytesTruncatedInfo(5))
        )
        val payloadProto = ProductFlowEventProto.Payload()
        whenever(payloadConverter.convert(payload)).thenReturn(
            TrimmingResult(payloadProto, BytesTruncatedInfo(6))
        )

        val result = converter.convert(event)

        assertThat(result.bytesTruncated).isEqualTo(21)
        ProtoObjectPropertyAssertions(result.value)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_STEP)
            .checkField("productId", getUTF8Bytes("product-1"))
            .checkField("productOfferId", getUTF8Bytes("offer-1"))
            .checkField("price", amountProto)
            .checkField("payload", payloadProto)
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
    fun `convert handles null optional fields`() {
        val event = FlowStepEvent(
            productId = "product-1",
            productOfferId = null,
            stepType = "step",
            stepOption = null,
            price = null,
            payload = null,
        )

        whenever(stringTrimmer.trim("product-1")).thenReturn(TrimmingResult("product-1", BytesTruncatedInfo(0)))
        whenever(stringTrimmer.trim(null)).thenReturn(TrimmingResult(null, BytesTruncatedInfo(0)))
        whenever(stringTrimmer.trim("step")).thenReturn(TrimmingResult("step", BytesTruncatedInfo(3)))

        val result = converter.convert(event)

        assertThat(result.bytesTruncated).isEqualTo(3)
        ProtoObjectPropertyAssertions(result.value)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_STEP)
            .checkField("productId", getUTF8Bytes("product-1"))
            .checkField("productOfferId", getUTF8Bytes(""))
            .checkFieldIsNull("price")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("shownInfo")
            .checkFieldIsNull("resultInfo")
            .checkFieldRecursively(
                "stepInfo"
            ) { stepInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.StepInfo> ->
                stepInfoAssertions
                    .checkField("stepType", getUTF8Bytes("step"))
                    .checkField("stepOption", getUTF8Bytes(""))
            }
            .checkAll()
    }
}
