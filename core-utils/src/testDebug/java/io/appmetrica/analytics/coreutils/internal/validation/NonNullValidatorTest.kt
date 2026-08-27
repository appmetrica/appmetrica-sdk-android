package io.appmetrica.analytics.coreutils.internal.validation

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

class NonNullValidatorTest : CommonTest() {

    private val validator = NonNullValidator<Any>("test non null")

    @Test
    fun nonNullPasses() {
        assertThat(validator.validate(mock()).isValid).isTrue
    }

    @Test
    fun nullFails() {
        val result = validator.validate(null)
        assertThat(result.isValid).isFalse
        assertThat(result.description).startsWith("test non null")
    }
}
