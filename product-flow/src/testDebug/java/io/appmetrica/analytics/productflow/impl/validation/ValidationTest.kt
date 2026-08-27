package io.appmetrica.analytics.productflow.impl.validation

import io.appmetrica.analytics.productflow.IllegalProductFlowParametersException
import io.appmetrica.analytics.productflow.ProductFlowStatus
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

internal class ValidationTest : CommonTest() {

    @Test
    fun `validateProductFlowStatus rejects null`() {
        assertThatThrownBy { Validation.validateProductFlowStatus(null) }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("ProductFlowStatus must be NonNull")
    }

    @Test
    fun `validateProductFlowStatus accepts valid status`() {
        assertThatCode { Validation.validateProductFlowStatus(ProductFlowStatus.SUCCESS) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `validateProductId rejects null`() {
        assertThatThrownBy { Validation.validateProductId(null) }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("ProductId must be NonNull and not empty")
    }

    @Test
    fun `validateProductId rejects empty value`() {
        assertThatThrownBy { Validation.validateProductId("") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("ProductId must be NonNull and not empty")
    }

    @Test
    fun `validateProductId accepts valid value`() {
        assertThatCode { Validation.validateProductId("product") }
            .doesNotThrowAnyException()
    }

    @Test
    fun `validateProductOfferId rejects null`() {
        assertThatThrownBy { Validation.validateProductOfferId(null) }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("ProductOfferId must be NonNull and not empty")
    }

    @Test
    fun `validateProductOfferId rejects empty value`() {
        assertThatThrownBy { Validation.validateProductOfferId("") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("ProductOfferId must be NonNull and not empty")
    }

    @Test
    fun `validateProductOfferId accepts valid value`() {
        assertThatCode { Validation.validateProductOfferId("offer") }
            .doesNotThrowAnyException()
    }

    @Test
    fun `validateProductIdAndProductOfferId rejects null values`() {
        assertThatThrownBy { Validation.validateProductIdAndProductOfferId(null, null) }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("One of ProductId and ProductOfferId must be NonNull and not empty")
    }

    @Test
    fun `validateProductIdAndProductOfferId rejects empty values`() {
        assertThatThrownBy { Validation.validateProductIdAndProductOfferId("", "") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("One of ProductId and ProductOfferId must be NonNull and not empty")
    }

    @Test
    fun `validateProductIdAndProductOfferId accepts product id only`() {
        assertThatCode { Validation.validateProductIdAndProductOfferId("product", null) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `validateProductIdAndProductOfferId accepts product offer id only`() {
        assertThatCode { Validation.validateProductIdAndProductOfferId(null, "offer") }
            .doesNotThrowAnyException()
    }

    @Test
    fun `validateStepType rejects null`() {
        assertThatThrownBy { Validation.validateStepType(null) }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("StepType must be NonNull and not empty")
    }

    @Test
    fun `validateStepType rejects empty value`() {
        assertThatThrownBy { Validation.validateStepType("") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("StepType must be NonNull and not empty")
    }

    @Test
    fun `validateStepType accepts valid value`() {
        assertThatCode { Validation.validateStepType("documents") }
            .doesNotThrowAnyException()
    }

    @Test
    fun `validateOfferType rejects null`() {
        assertThatThrownBy { Validation.validateOfferType(null) }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("OfferType must be NonNull and not empty")
    }

    @Test
    fun `validateOfferType rejects empty value`() {
        assertThatThrownBy { Validation.validateOfferType("") }
            .isInstanceOf(IllegalProductFlowParametersException::class.java)
            .hasMessage("OfferType must be NonNull and not empty")
    }

    @Test
    fun `validateOfferType accepts valid value`() {
        assertThatCode { Validation.validateOfferType("financial_product") }
            .doesNotThrowAnyException()
    }
}
