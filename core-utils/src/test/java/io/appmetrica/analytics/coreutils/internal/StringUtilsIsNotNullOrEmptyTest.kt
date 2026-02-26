package io.appmetrica.analytics.coreutils.internal

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class StringUtilsIsNotNullOrEmptyTest(
    private val value: String?,
    private val expected: Boolean
) : CommonTest() {

    companion object {

        @Parameterized.Parameters(name = "for \"{0}\" will be {1}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(null, false),
            arrayOf("", false),
            arrayOf(" ", true),
            arrayOf("test", true),
            arrayOf("  ", true),
            arrayOf("\t", true),
            arrayOf("\n", true),
            arrayOf("a", true),
            arrayOf("null", true)
        )
    }

    @Test
    fun isNotNullOrEmpty() {
        assertThat(StringUtils.isNotNullOrEmpty(value)).isEqualTo(expected)
    }
}
