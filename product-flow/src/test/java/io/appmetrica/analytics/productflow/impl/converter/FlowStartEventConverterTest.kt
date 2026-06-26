package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils.getUTF8Bytes
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.analytics.productflow.impl.events.model.FlowStartEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class FlowStartEventConverterTest : CommonTest() {

    private val offerPriceConverter: OfferPriceConverter = mock()
    private val payloadConverter: PayloadConverter = mock()
    private val stringTrimmer: HierarchicalStringTrimmer = mock()
    private val converter = FlowStartEventConverter(
        offerPriceConverter,
        payloadConverter,
        stringTrimmer,
    )

    @Test
    fun `convert maps flow start event`() {
        val price = OfferPrice(BigDecimal.TEN, "USD")
        val payload = mapOf("source" to "push")
        val event = FlowStartEvent("product-1", "offer-1", price, payload)

        whenever(stringTrimmer.trim("product-1")).thenReturn(TrimmingResult("product-1", BytesTruncatedInfo(2)))
        whenever(stringTrimmer.trim("offer-1")).thenReturn(TrimmingResult("offer-1", BytesTruncatedInfo(3)))

        val amountProto = ProductFlowEventProto.Amount()
        whenever(offerPriceConverter.convert(price)).thenReturn(
            TrimmingResult(amountProto, BytesTruncatedInfo(5))
        )
        val payloadProto = ProductFlowEventProto.Payload()
        whenever(payloadConverter.convert(payload)).thenReturn(
            TrimmingResult(payloadProto, BytesTruncatedInfo(7))
        )

        val result = converter.convert(event)

        assertThat(result.bytesTruncated).isEqualTo(17)
        ProtoObjectPropertyAssertions(result.value)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_FLOW_START)
            .checkField("productId", getUTF8Bytes("product-1"))
            .checkField("productOfferId", getUTF8Bytes("offer-1"))
            .checkField("price", amountProto)
            .checkField("payload", payloadProto)
            .checkFieldIsNull("shownInfo")
            .checkFieldIsNull("stepInfo")
            .checkFieldIsNull("resultInfo")
            .checkAll()
    }

    @Test
    fun `convert handles null optional fields`() {
        val event = FlowStartEvent("product-1", null, null, null)

        whenever(stringTrimmer.trim("product-1")).thenReturn(TrimmingResult("product-1", BytesTruncatedInfo(0)))
        whenever(stringTrimmer.trim(null)).thenReturn(TrimmingResult(null, BytesTruncatedInfo(0)))

        val result = converter.convert(event)

        assertThat(result.bytesTruncated).isZero()
        ProtoObjectPropertyAssertions(result.value)
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
