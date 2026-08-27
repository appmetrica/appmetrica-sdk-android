package io.appmetrica.analytics.productflow

import io.appmetrica.analytics.productflow.impl.ProductFlowAppMetricaEvent
import io.appmetrica.analytics.productflow.impl.events.eventdata.OfferShownEventData
import io.appmetrica.analytics.productflow.impl.events.model.OfferShownEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Rule
import org.junit.Test

internal class OfferShownEventBuilderTest : CommonTest() {

    @get:Rule
    val offerShownEventDataRule = constructionRule<OfferShownEventData>()

    @Test
    fun `build returns product flow app metrica event with configured fields`() {
        val price = OfferPrice(100, "USD")
        val referrer = OfferReferrer.newBuilder().withType("banner").build()
        val payload = mapOf("campaign" to "spring")

        val event = ProductFlowEvent.offerShown("offer-1", "financial_product")
            .withProductId("product-1")
            .withBenefitType("cashback")
            .withPrice(price)
            .withPayload(payload)
            .withReferrer(referrer)
            .build()

        val eventData = offerShownEventDataRule.single { args ->
            ObjectPropertyAssertions(args.single() as OfferShownEvent)
                .checkField("productOfferId", "offer-1")
                .checkField("offerType", "financial_product")
                .checkField("productId", "product-1")
                .checkField("benefitType", "cashback")
                .checkField("price", price)
                .checkField("payload", payload)
                .checkField("referrer", referrer)
                .checkAll()
        }

        assertThat(event).isInstanceOf(ProductFlowAppMetricaEvent::class.java)
        assertThat(event.eventData).isSameAs(eventData)
    }

    @Test
    fun `constructor validates required productOfferId`() {
        assertThatThrownBy { ProductFlowEvent.offerShown("", "financial_product") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("ProductOfferId must be NonNull and not empty")
    }

    @Test
    fun `constructor validates required offerType`() {
        assertThatThrownBy { ProductFlowEvent.offerShown("offer-1", "") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("OfferType must be NonNull and not empty")
    }
}
