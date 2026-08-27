package io.appmetrica.analytics.coreutils.internal.validation

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DummyValidatorTest : CommonTest() {

    private val validator = DummyValidator<Any>()

    @Test
    fun nullAndNonNull() {
        assertThat(validator.validate(null).isValid).isTrue
        assertThat(validator.validate(Any()).isValid).isTrue
    }
}
