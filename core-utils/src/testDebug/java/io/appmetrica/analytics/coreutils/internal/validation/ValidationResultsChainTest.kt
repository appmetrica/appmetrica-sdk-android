package io.appmetrica.analytics.coreutils.internal.validation

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

class ValidationResultsChainTest : CommonTest() {

    private val chain = ValidationResultsChain()
    private val validator = mock<Validator<*>>()

    @Test
    fun allValid() {
        val result = chain.validate(List(4) { ValidationResult.successful(validator) })
        assertThat(result.isValid).isTrue
    }

    @Test
    fun oneInvalid() {
        val result = chain.validate(
            listOf(
                ValidationResult.successful(validator),
                ValidationResult.successful(validator),
                ValidationResult.failed(validator, "error"),
                ValidationResult.successful(validator)
            )
        )
        assertThat(result.isValid).isFalse
        assertThat(result.description).isEqualTo("error")
    }

    @Test
    fun allInvalid() {
        val result = chain.validate(
            listOf(
                ValidationResult.failed(validator, "error1"),
                ValidationResult.failed(validator, "error2"),
                ValidationResult.failed(validator, "error3"),
                ValidationResult.failed(validator, "error4")
            )
        )
        assertThat(result.isValid).isFalse
        assertThat(result.description).isEqualTo("error1, error2, error3, error4")
    }
}
