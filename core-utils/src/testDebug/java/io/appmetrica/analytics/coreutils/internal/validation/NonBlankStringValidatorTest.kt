package io.appmetrica.analytics.coreutils.internal.validation

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class NonBlankStringValidatorTest(
    private val caseName: String,
    private val value: String?,
    private val isValid: Boolean
) : CommonTest() {

    private val validator = NonBlankStringValidator("someTestObject")

    @Test
    fun validate() {
        val result = validator.validate(value)
        val expectedDescription = if (isValid) "" else "someTestObject is null, empty or blank."

        SoftAssertions().apply {
            assertThat(result.isValid).`as`("$caseName: isValid").isEqualTo(isValid)
            assertThat(result.description).`as`("$caseName: description").isEqualTo(expectedDescription)
            assertThat(result.validatorClass).`as`("$caseName: validatorClass").isSameAs(validator.javaClass)
            assertAll()
        }
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf("null", null, false),
            arrayOf("empty", "", false),
            arrayOf("space", " ", false),
            arrayOf("ASCII whitespace", "\t\n\r\u000B\u000C", false),
            arrayOf("NBSP U+00A0", "\u00A0", false),
            arrayOf("em space U+2003", "\u2003", false),
            arrayOf("zero width space U+200B", "\u200B", true),
            arrayOf("normal", "string", true),
            arrayOf("edge spaces", " string ", true)
        )
    }
}
