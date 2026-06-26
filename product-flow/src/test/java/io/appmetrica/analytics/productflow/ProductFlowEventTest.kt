package io.appmetrica.analytics.productflow

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

internal class ProductFlowEventTest : CommonTest() {

    @get:Rule
    val offerShownEventBuilderRule = constructionRule<OfferShownEventBuilder>()

    @get:Rule
    val flowStartEventBuilderRule = constructionRule<FlowStartEventBuilder>()

    @get:Rule
    val flowStepEventBuilderRule = constructionRule<FlowStepEventBuilder>()

    @get:Rule
    val flowResultEventBuilderRule = constructionRule<FlowResultEventBuilder>()

    @Test
    fun `offerShown creates OfferShownEventBuilder with correct parameters`() {
        val builder = ProductFlowEvent.offerShown("offer-1", "financial_product")

        assertThat(builder).isSameAs(
            offerShownEventBuilderRule.singleWithArgs("offer-1", "financial_product"),
        )
    }

    @Test
    fun `flowStart creates FlowStartEventBuilder with correct parameters`() {
        val builder = ProductFlowEvent.flowStart("product-1")

        assertThat(builder).isSameAs(
            flowStartEventBuilderRule.singleWithArgs("product-1"),
        )
    }

    @Test
    fun `flowStepForProduct creates FlowStepEventBuilder with correct parameters`() {
        val builder = ProductFlowEvent.flowStepForProduct("product-1", "documents")

        assertThat(builder).isSameAs(
            flowStepEventBuilderRule.singleWithArgs("documents", "product-1", null),
        )
    }

    @Test
    fun `flowStepForOffer creates FlowStepEventBuilder with correct parameters`() {
        val builder = ProductFlowEvent.flowStepForOffer("offer-1", "documents")

        assertThat(builder).isSameAs(
            flowStepEventBuilderRule.singleWithArgs("documents", null, "offer-1"),
        )
    }

    @Test
    fun `flowResultForProduct creates FlowResultEventBuilder with correct parameters`() {
        val builder = ProductFlowEvent.flowResultForProduct(ProductFlowStatus.SUCCESS, "product-1")

        assertThat(builder).isSameAs(
            flowResultEventBuilderRule.singleWithArgs(ProductFlowStatus.SUCCESS, "product-1", null),
        )
    }

    @Test
    fun `flowResultForOffer creates FlowResultEventBuilder with correct parameters`() {
        val builder = ProductFlowEvent.flowResultForOffer(ProductFlowStatus.FAIL, "offer-1")

        assertThat(builder).isSameAs(
            flowResultEventBuilderRule.singleWithArgs(ProductFlowStatus.FAIL, null, "offer-1"),
        )
    }
}
