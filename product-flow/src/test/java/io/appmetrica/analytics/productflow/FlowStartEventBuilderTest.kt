package io.appmetrica.analytics.productflow

import io.appmetrica.analytics.productflow.impl.ProductFlowAppMetricaEvent
import io.appmetrica.analytics.productflow.impl.events.eventdata.FlowStartEventData
import io.appmetrica.analytics.productflow.impl.events.model.FlowStartEvent
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Rule
import org.junit.Test

internal class FlowStartEventBuilderTest : CommonTest() {

    @get:Rule
    val flowStartEventDataRule = constructionRule<FlowStartEventData>()

    @Test
    fun `build returns product flow app metrica event with configured fields`() {
        val price = OfferPrice(10, "USD")
        val payload = mapOf("source" to "push")

        val event = ProductFlowEvent.flowStart("product-1")
            .withProductOfferId("offer-1")
            .withPrice(price)
            .withPayload(payload)
            .build()

        val eventData = flowStartEventDataRule.single { args ->
            ObjectPropertyAssertions(args.single() as FlowStartEvent)
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
    fun `constructor validates product id`() {
        assertThatThrownBy { ProductFlowEvent.flowStart("") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("ProductId must be NonNull and not empty")
    }
}
