package io.appmetrica.analytics.coreutils.internal.validation

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NonEmptyStringValidatorTest : CommonTest() {

    private val validator = NonEmptyStringValidator("someTestObject")

    @Test
    fun nullAndEmptyFail() {
        assertThat(validator.validate(null).isValid).isFalse
        assertThat(validator.validate("").isValid).isFalse
    }

    @Test
    fun nonEmptyPasses() {
        assertThat(validator.validate("string").isValid).isTrue
    }
}
