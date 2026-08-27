package io.appmetrica.analytics.coreutils.internal.validation

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.mockito.kotlin.mock

class ValidationResultTest : CommonTest() {

    @Test
    fun successful() {
        val validator = mock<Validator<*>>()
        val result = ValidationResult.successful(validator)

        SoftAssertions().apply {
            assertThat(result.isValid).`as`("result").isTrue
            assertThat(result.description).`as`("description").isEmpty()
            assertThat(result.validatorClass).`as`("validatorClass").isSameAs(validator.javaClass)
            assertAll()
        }
    }

    @Test
    fun failed() {
        val validator = mock<Validator<*>>()
        val result = ValidationResult.failed(validator, "error")

        SoftAssertions().apply {
            assertThat(result.isValid).`as`("result").isFalse
            assertThat(result.description).`as`("description").isEqualTo("error")
            assertThat(result.validatorClass).`as`("validatorClass").isSameAs(validator.javaClass)
            assertAll()
        }
    }
}
