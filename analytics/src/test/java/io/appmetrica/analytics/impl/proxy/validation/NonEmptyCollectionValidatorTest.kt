package io.appmetrica.analytics.impl.proxy.validation

import io.appmetrica.analytics.impl.utils.validation.NonEmptyCollectionValidator
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class NonEmptyCollectionValidatorTest : CommonTest() {

    private val validator = NonEmptyCollectionValidator<String>("My collection")

    @Test
    fun nullCollection() {
        val result = validator.validate(null)
        assertThat(result.isValid).isFalse
        assertThat(result.description).isEqualTo("My collection is null or empty.")
        assertThat(result.validatorClass).isEqualTo(NonEmptyCollectionValidator::class.java)
    }

    @Test
    fun emptyCollection() {
        val result = validator.validate(emptyList())
        assertThat(result.isValid).isFalse
        assertThat(result.description).isEqualTo("My collection is null or empty.")
        assertThat(result.validatorClass).isEqualTo(NonEmptyCollectionValidator::class.java)
    }

    @Test
    fun filledCollection() {
        val result = validator.validate(listOf("item"))
        assertThat(result.isValid).isTrue
        assertThat(result.description).isEmpty()
        assertThat(result.validatorClass).isEqualTo(NonEmptyCollectionValidator::class.java)
    }
}
