package io.appmetrica.analytics.coreutils.internal.validation

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class IntegerFromRangeValidatorTest(
    private val input: Int?,
    private val possibleValues: List<Int>,
    private val valid: Boolean
) : CommonTest() {

    companion object {
        private val ONE = listOf(100)
        private val EMPTY = emptyList<Int>()
        private val MANY = listOf(100, 101, 102, 110, 111, 200, 300, 400)

        @Parameterized.Parameters(name = "[{index}] valid={2} for input={0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(100, ONE, true),
            arrayOf(200, ONE, false),
            arrayOf(100, EMPTY, false),
            arrayOf(102, MANY, true),
            arrayOf(103, MANY, false),
            arrayOf(null, EMPTY, false),
            arrayOf(null, MANY, false)
        )
    }

    private val validator = IntegerFromRangeValidator("Description", possibleValues)

    @Test
    fun validate() {
        val result = validator.validate(input)
        assertThat(result.isValid).isEqualTo(valid)
        if (!valid) {
            assertThat(result.description).contains("Description")
        }
    }
}
