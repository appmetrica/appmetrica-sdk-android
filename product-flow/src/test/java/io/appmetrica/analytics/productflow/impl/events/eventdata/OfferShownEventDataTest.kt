package io.appmetrica.analytics.productflow.impl.events.eventdata

import io.appmetrica.analytics.coreutils.internal.StringUtils.getUTF8Bytes
import io.appmetrica.analytics.coreutils.internal.proto.DecimalProtoModel
import io.appmetrica.analytics.productflow.OfferPrice
import io.appmetrica.analytics.productflow.OfferReferrer
import io.appmetrica.analytics.productflow.ProductFlowEvent
import io.appmetrica.analytics.productflow.impl.ProductFlowConstants
import io.appmetrica.analytics.productflow.impl.events.model.OfferShownEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal
import io.appmetrica.analytics.productflow.impl.protobuf.backend.ProductFlowEvent as ProductFlowEventProto

internal class OfferShownEventDataTest : CommonTest() {

    @Test
    fun `serializes offer shown event to protobuf`() {
        val price = OfferPrice(BigDecimal.ONE, "USD")
        val event = OfferShownEvent(
            productOfferId = "offer-1",
            offerType = "financial_product",
            productId = "product-1",
            benefitType = "cashback",
            price = price,
            payload = mapOf("campaign" to "spring"),
            referrer = OfferReferrer.newBuilder()
                .withType("banner")
                .withIdentifier("id-1")
                .withScreen("home")
                .build(),
        )
        val eventData = OfferShownEventData(event)

        assertThat(eventData.type).isEqualTo(ProductFlowConstants.CUSTOM_EVENT_TYPE)
        assertThat(eventData.bytesTruncated).isZero()
        assertThat(eventData.description).contains("offer-1", "financial_product", "product-1")

        val proto = ProductFlowEventProto.parseFrom(eventData.data)
        ProtoObjectPropertyAssertions(proto)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_OFFER_SHOWN)
            .checkField("productOfferId", getUTF8Bytes("offer-1"))
            .checkField("productId", getUTF8Bytes("product-1"))
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
                                key = getUTF8Bytes("campaign")
                                value = getUTF8Bytes("spring")
                            },
                        ),
                    )
            }
            .checkFieldRecursively(
                "shownInfo"
            ) { shownInfoAssertions: ObjectPropertyAssertions<ProductFlowEventProto.ShownInfo> ->
                shownInfoAssertions
                    .checkField("offerType", getUTF8Bytes("financial_product"))
                    .checkField("benefitType", getUTF8Bytes("cashback"))
                    .checkFieldRecursively(
                        "referrer"
                    ) { referrerAssertions: ObjectPropertyAssertions<ProductFlowEventProto.Referrer> ->
                        referrerAssertions
                            .checkField("type", getUTF8Bytes("banner"))
                            .checkField("identifier", getUTF8Bytes("id-1"))
                            .checkField("screen", getUTF8Bytes("home"))
                    }
            }
            .checkAll()
    }

    @Test
    fun `truncates long strings and reports bytes truncated`() {
        val longValue = "x".repeat(ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE + 100)
        val event = OfferShownEvent(
            productOfferId = longValue,
            offerType = "financial_product",
            productId = null,
            benefitType = null,
            price = null,
            payload = null,
            referrer = null,
        )
        val eventData = OfferShownEventData(event)

        assertThat(eventData.bytesTruncated).isPositive()

        val proto = ProductFlowEventProto.parseFrom(eventData.data)
        ProtoObjectPropertyAssertions(proto)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_OFFER_SHOWN)
            .checkField(
                "productOfferId",
                getUTF8Bytes("x".repeat(ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE)),
            )
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

    @Test
    fun `truncates cyrillic product offer id by character limit and reports bytes truncated`() {
        val fittingCyrillic = "ы".repeat(ProductFlowConstants.PRODUCT_FLOW_GENERIC_STRING_MAX_SIZE)
        val event = OfferShownEvent(
            productOfferId = fittingCyrillic + "кошка",
            offerType = "financial_product",
            productId = null,
            benefitType = null,
            price = null,
            payload = null,
            referrer = null,
        )
        val eventData = OfferShownEventData(event)

        assertThat(eventData.bytesTruncated).isEqualTo(10)

        val proto = ProductFlowEventProto.parseFrom(eventData.data)
        ProtoObjectPropertyAssertions(proto)
            .checkField("eventType", ProductFlowEventProto.EVENT_TYPE_OFFER_SHOWN)
            .checkField("productOfferId", getUTF8Bytes(fittingCyrillic))
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

    @Test
    fun `builder produces same event data`() {
        val appMetricaEvent = ProductFlowEvent.offerShown("offer-1", "financial_product")
            .withProductId("product-1")
            .build()

        assertThat(appMetricaEvent.eventData).isInstanceOf(OfferShownEventData::class.java)
        assertThat(appMetricaEvent.eventData.type).isEqualTo(ProductFlowConstants.CUSTOM_EVENT_TYPE)
    }
}
