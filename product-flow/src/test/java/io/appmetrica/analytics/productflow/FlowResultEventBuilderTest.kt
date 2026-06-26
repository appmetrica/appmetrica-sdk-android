package io.appmetrica.analytics.productflow

import io.appmetrica.analytics.productflow.impl.ProductFlowAppMetricaEvent
import io.appmetrica.analytics.productflow.impl.events.eventdata.FlowResultEventData
import io.appmetrica.analytics.productflow.impl.events.model.FlowResultEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Rule
import org.junit.Test

internal class FlowResultEventBuilderTest : CommonTest() {

    @get:Rule
    val flowResultEventDataRule = constructionRule<FlowResultEventData>()

    @Test
    fun `build for product returns event with configured fields`() {
        val price = OfferPrice(100, "USD")
        val payload = mapOf("source" to "push")

        val event = ProductFlowEvent.flowResultForProduct(ProductFlowStatus.SUCCESS, "product-1")
            .withProductOfferId("offer-1")
            .withPrice(price)
            .withPayload(payload)
            .build()

        val eventData = flowResultEventDataRule.single { args ->
            ObjectPropertyAssertions(args.single() as FlowResultEvent)
                .checkField("status", ProductFlowStatus.SUCCESS)
                .checkField("productId", "product-1")
                .checkField("productOfferId", "offer-1")
                .checkField("price", price)
                .checkField("payload", payload)
                .checkAll()
        }

        assertThat(event).isInstanceOf(ProductFlowAppMetricaEvent::class.java)
        assertThat(event.eventData).isSameAs(eventData)
    }

    @Test
    fun `build for offer returns event with configured fields`() {
        val price = OfferPrice(0, "USD")
        val payload = mapOf("step" to "confirm")

        val event = ProductFlowEvent.flowResultForOffer(ProductFlowStatus.FAIL, "offer-1")
            .withProductId("product-1")
            .withPrice(price)
            .withPayload(payload)
            .build()

        val eventData = flowResultEventDataRule.single { args ->
            ObjectPropertyAssertions(args.single() as FlowResultEvent)
                .checkField("status", ProductFlowStatus.FAIL)
                .checkField("productId", "product-1")
                .checkField("productOfferId", "offer-1")
                .checkField("price", price)
                .checkField("payload", payload)
                .checkAll()
        }

        assertThat(event).isInstanceOf(ProductFlowAppMetricaEvent::class.java)
        assertThat(event.eventData).isSameAs(eventData)
    }

    @Test
    fun `constructor validates required product id`() {
        assertThatThrownBy { ProductFlowEvent.flowResultForProduct(ProductFlowStatus.SUCCESS, "") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("One of ProductId and ProductOfferId must be NonNull and not empty")
    }

    @Test
    fun `withProductOfferId validates product offer id`() {
        val builder = ProductFlowEvent.flowResultForOffer(ProductFlowStatus.PENDING, "offer-1")

        assertThatThrownBy { builder.withProductOfferId("") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("One of ProductId and ProductOfferId must be NonNull and not empty")
    }

    @Test
    fun `withProductId validates product id`() {
        val builder = ProductFlowEvent.flowResultForProduct(ProductFlowStatus.CANCELLED, "product-1")

        assertThatThrownBy { builder.withProductId("") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("One of ProductId and ProductOfferId must be NonNull and not empty")
    }
}
