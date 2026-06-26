package io.appmetrica.analytics.productflow.impl.converter

import io.appmetrica.analytics.coreutils.internal.StringUtils.getUTF8Bytes
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.coreutils.internal.limitation.TrimmingResult
import io.appmetrica.analytics.coreutils.internal.limitation.hierarchical.HierarchicalStringTrimmer
import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.analytics.productflow.OfferReferrer
import io.appmetrica.analytics.productflow.impl.events.model.OfferShownEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class OfferShownEventConverterTest : CommonTest() {

    private val offerPriceConverter: OfferPriceConverter = mock()
    private val payloadConverter: PayloadConverter = mock()
    private val offerReferrerConverter: OfferReferrerConverter = mock()
    private val stringTrimmer: HierarchicalStringTrimmer = mock()
    private val converter = OfferShownEventConverter(
        offerPriceConverter,
        payloadConverter,
        offerReferrerConverter,
        stringTrimmer,
    )

    @Test
    fun `convert maps full offer shown event and sums bytes truncated`() {
        val price = OfferPrice(BigDecimal.ONE, "USD")
        val payload = mapOf("k" to "v")
        val referrer = OfferReferrer.newBuilder().withType("banner").build()
        val event = OfferShownEvent(
            productOfferId = "offer-1",
            offerType = "financial_product",
            productId = "product-1",
            benefitType = "cashback",
            price = price,
            payload = payload,
            referrer = referrer,
        )

        whenever(stringTrimmer.trim("offer-1")).thenReturn(TrimmingResult("offer-1", BytesTruncatedInfo(1)))
        whenever(stringTrimmer.trim("product-1")).thenReturn(TrimmingResult("product-1", BytesTruncatedInfo(2)))
        whenever(stringTrimmer.trim("financial_product"))
            .thenReturn(TrimmingResult("financial_product", BytesTruncatedInfo(3)))
        whenever(stringTrimmer.trim("cashback")).thenReturn(TrimmingResult("cashback", BytesTruncatedInfo(4)))

        val amountProto = ProductFlowEventProto.Amount()
        whenever(offerPriceConverter.convert(price)).thenReturn(
            TrimmingResult(amountProto, BytesTruncatedInfo(5))
        )
        val payloadProto = ProductFlowEventProto.Payload()
        whenever(payloadConverter.convert(payload)).thenReturn(
            TrimmingResult(payloadProto, BytesTruncatedInfo(6))
        )
        val referrerProto = ProductFlowEventProto.Referrer()
        whenever(offerReferrerConverter.convert(referrer)).thenReturn(
            TrimmingResult(referrerProto, BytesTruncatedInfo(7))
        )

        val result = converter.convert(event)

        assertThat(result.bytesTruncated).isEqualTo(28)
        ProtoObjectPropertyAssertions(result.value)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_OFFER_SHOWN)
            .checkField("productOfferId", getUTF8Bytes("offer-1"))
            .checkField("productId", getUTF8Bytes("product-1"))
            .checkField("price", amountProto)
            .checkField("payload", payloadProto)
            .checkFieldIsNull("stepInfo")
            .checkFieldIsNull("resultInfo")
            .checkFieldRecursively(
                "shownInfo"
            ) { shownInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.ShownInfo> ->
                shownInfoAssertions
                    .checkField("offerType", getUTF8Bytes("financial_product"))
                    .checkField("benefitType", getUTF8Bytes("cashback"))
                    .checkField("referrer", referrerProto)
            }
            .checkAll()
    }

    @Test
    fun `convert handles null optional fields`() {
        val event = OfferShownEvent(
            productOfferId = "offer-1",
            offerType = "financial_product",
            productId = null,
            benefitType = null,
            price = null,
            payload = null,
            referrer = null,
        )

        whenever(stringTrimmer.trim("offer-1")).thenReturn(TrimmingResult("offer-1", BytesTruncatedInfo(0)))
        whenever(stringTrimmer.trim(null)).thenReturn(TrimmingResult(null, BytesTruncatedInfo(0)))
        whenever(stringTrimmer.trim("financial_product"))
            .thenReturn(TrimmingResult("financial_product", BytesTruncatedInfo(0)))

        val result = converter.convert(event)

        assertThat(result.bytesTruncated).isZero()
        ProtoObjectPropertyAssertions(result.value)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_OFFER_SHOWN)
            .checkField("productOfferId", getUTF8Bytes("offer-1"))
            .checkField("productId", getUTF8Bytes(null as String?))
            .checkFieldIsNull("price")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("stepInfo")
            .checkFieldIsNull("resultInfo")
            .checkFieldRecursively(
                "shownInfo"
            ) { shownInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.ShownInfo> ->
                shownInfoAssertions
                    .checkField("offerType", getUTF8Bytes("financial_product"))
                    .checkField("benefitType", getUTF8Bytes(null as String?))
                    .checkFieldIsNull("referrer")
            }
            .checkAll()
    }
}
