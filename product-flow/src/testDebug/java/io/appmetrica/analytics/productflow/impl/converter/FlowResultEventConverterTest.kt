package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils.getUTF8Bytes
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.analytics.productflow.ProductFlowStatus
import io.appmetrica.analytics.productflow.impl.events.model.FlowResultEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class FlowResultEventConverterTest : CommonTest() {

    private val offerPriceConverter: OfferPriceConverter = mock()
    private val payloadConverter: PayloadConverter = mock()
    private val stringTrimmer: HierarchicalStringTrimmer = mock()
    private val converter = FlowResultEventConverter(
        offerPriceConverter,
        payloadConverter,
        stringTrimmer,
    )

    @Test
    fun `convert maps flow result event`() {
        val price = OfferPrice(BigDecimal("50"), "USD")
        val payload = mapOf("reason" to "approved")
        val event = FlowResultEvent(ProductFlowStatus.SUCCESS, "product-1", "offer-1", price, payload)

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
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_FLOW_RESULT)
            .checkField("productId", getUTF8Bytes("product-1"))
            .checkField("productOfferId", getUTF8Bytes("offer-1"))
            .checkField("price", amountProto)
            .checkField("payload", payloadProto)
            .checkFieldIsNull("shownInfo")
            .checkFieldIsNull("stepInfo")
            .checkFieldRecursively(
                "resultInfo"
            ) { resultInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.ResultInfo> ->
                resultInfoAssertions
                    .checkField("status", ProductFlowEventProto.ResultInfo.STATUS_SUCCESS)
            }
            .checkAll()
    }

    @Test
    fun `convert handles null optional fields`() {
        val event = FlowResultEvent(ProductFlowStatus.SUCCESS, "product-1", null, null, null)

        whenever(stringTrimmer.trim("product-1")).thenReturn(TrimmingResult("product-1", BytesTruncatedInfo(0)))
        whenever(stringTrimmer.trim(null)).thenReturn(TrimmingResult(null, BytesTruncatedInfo(0)))

        val result = converter.convert(event)

        assertThat(result.bytesTruncated).isZero()
        ProtoObjectPropertyAssertions(result.value)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_FLOW_RESULT)
            .checkField("productId", getUTF8Bytes("product-1"))
            .checkField("productOfferId", getUTF8Bytes(null as String?))
            .checkFieldIsNull("price")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("shownInfo")
            .checkFieldIsNull("stepInfo")
            .checkFieldRecursively(
                "resultInfo"
            ) { resultInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.ResultInfo> ->
                resultInfoAssertions
                    .checkField("status", ProductFlowEventProto.ResultInfo.STATUS_SUCCESS)
            }
            .checkAll()
    }

    @RunWith(Parameterized::class)
    internal class StatusMappingTest(
        private val status: ProductFlowStatus,
        private val protoStatus: Int,
    ) : CommonTest() {

        private val offerPriceConverter: OfferPriceConverter = mock()
        private val payloadConverter: PayloadConverter = mock()
        private val stringTrimmer: HierarchicalStringTrimmer = mock()
        private val converter = FlowResultEventConverter(
            offerPriceConverter,
            payloadConverter,
            stringTrimmer,
        )

        companion object {
            @Parameterized.Parameters(name = "status={0} -> protoStatus={1}")
            @JvmStatic
            fun data(): Collection<Array<Any>> = listOf(
                arrayOf(ProductFlowStatus.SUCCESS, ProductFlowEventProto.ResultInfo.STATUS_SUCCESS),
                arrayOf(ProductFlowStatus.DECLINED, ProductFlowEventProto.ResultInfo.STATUS_DECLINED),
                arrayOf(ProductFlowStatus.PENDING, ProductFlowEventProto.ResultInfo.STATUS_PENDING),
                arrayOf(ProductFlowStatus.CANCELLED, ProductFlowEventProto.ResultInfo.STATUS_CANCELLED),
                arrayOf(ProductFlowStatus.EXPIRED, ProductFlowEventProto.ResultInfo.STATUS_EXPIRED),
                arrayOf(ProductFlowStatus.FAIL, ProductFlowEventProto.ResultInfo.STATUS_FAIL),
            )
        }

        @Test
        fun `convert maps status to proto`() {
            whenever(stringTrimmer.trim("product")).thenReturn(TrimmingResult("product", BytesTruncatedInfo(0)))
            whenever(stringTrimmer.trim(null)).thenReturn(TrimmingResult(null, BytesTruncatedInfo(0)))

            val result = converter.convert(FlowResultEvent(status, "product", null, null, null))

            ProtoObjectPropertyAssertions(result.value)
                .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_FLOW_RESULT)
                .checkField("productId", getUTF8Bytes("product"))
                .checkField("productOfferId", getUTF8Bytes(null as String?))
                .checkFieldIsNull("price")
                .checkFieldIsNull("payload")
                .checkFieldIsNull("shownInfo")
                .checkFieldIsNull("stepInfo")
                .checkFieldRecursively(
                    "resultInfo"
                ) { resultInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.ResultInfo> ->
                    resultInfoAssertions
                        .checkField("status", protoStatus)
                }
                .checkAll()
        }
    }
}
