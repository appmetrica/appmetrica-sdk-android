package io.appmetrica.analytics.productflow

import io.appmetrica.analytics.productflow.impl.ProductFlowAppMetricaEvent
import io.appmetrica.analytics.productflow.impl.events.eventdata.FlowStepEventData
import io.appmetrica.analytics.productflow.impl.events.model.FlowStepEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Rule
import org.junit.Test

internal class FlowStepEventBuilderTest : CommonTest() {

    @get:Rule
    val flowStepEventDataRule = constructionRule<FlowStepEventData>()

    @Test
    fun `build for product returns event with configured fields`() {
        val price = OfferPrice(50, "USD")
        val payload = mapOf("key" to "value")

        val event = ProductFlowEvent.flowStepForProduct("product-1", "documents")
            .withStepOption("passport")
            .withPrice(price)
            .withPayload(payload)
            .withProductOfferId("offer-1")
            .build()

        val eventData = flowStepEventDataRule.single { args ->
            ObjectPropertyAssertions(args.single() as FlowStepEvent)
                .checkField("productId", "product-1")
                .checkField("productOfferId", "offer-1")
                .checkField("stepType", "documents")
                .checkField("stepOption", "passport")
                .checkField("price", price)
                .checkField("payload", payload)
                .checkAll()
        }

        assertThat(event).isInstanceOf(ProductFlowAppMetricaEvent::class.java)
        assertThat(event.eventData).isSameAs(eventData)
    }

    @Test
    fun `build for offer returns event with configured fields`() {
        val price = OfferPrice(10, "RUB")
        val payload = mapOf("step" to "confirm")

        val event = ProductFlowEvent.flowStepForOffer("offer-1", "payment")
            .withProductId("product-1")
            .withPrice(price)
            .withPayload(payload)
            .build()

        val eventData = flowStepEventDataRule.single { args ->
            ObjectPropertyAssertions(args.single() as FlowStepEvent)
                .checkField("productId", "product-1")
                .checkField("productOfferId", "offer-1")
                .checkField("stepType", "payment")
                .checkFieldIsNull("stepOption")
                .checkField("price", price)
                .checkField("payload", payload)
                .checkAll()
        }

        assertThat(event).isInstanceOf(ProductFlowAppMetricaEvent::class.java)
        assertThat(event.eventData).isSameAs(eventData)
    }

    @Test
    fun `constructor validates required step type`() {
        assertThatThrownBy { ProductFlowEvent.flowStepForProduct("product-1", "") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("StepType must be NonNull and not empty")
    }

    @Test
    fun `constructor validates required product id`() {
        assertThatThrownBy { ProductFlowEvent.flowStepForProduct("", "documents") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("One of ProductId and ProductOfferId must be NonNull and not empty")
    }

    @Test
    fun `withProductId validates product id`() {
        val builder = ProductFlowEvent.flowStepForProduct("product-1", "documents")

        assertThatThrownBy { builder.withProductId("") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("One of ProductId and ProductOfferId must be NonNull and not empty")
    }

    @Test
    fun `withProductOfferId validates product offer id`() {
        val builder = ProductFlowEvent.flowStepForOffer("offer-1", "documents")

        assertThatThrownBy { builder.withProductOfferId("") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("One of ProductId and ProductOfferId must be NonNull and not empty")
    }
}
