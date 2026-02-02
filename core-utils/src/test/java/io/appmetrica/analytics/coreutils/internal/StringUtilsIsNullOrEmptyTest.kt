package io.appmetrica.analytics.coreutils.internal

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class StringUtilsIsNullOrEmptyTest(
    private val value: String?,
    private val expected: Boolean
) : CommonTest() {

    companion object {

        @Parameterized.Parameters(name = "for \"{0}\" will be {1}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(null, true),
            arrayOf("", true),
            arrayOf(" ", false),
            arrayOf("test", false),
            arrayOf("  ", false),
            arrayOf("\t", false),
            arrayOf("\n", false),
            arrayOf("a", false),
            arrayOf("null", false)
        )
    }

    @Test
    fun `isNullOrEmpty`() {
        assertThat(StringUtils.isNullOrEmpty(value)).isEqualTo(expected)
    }
}
