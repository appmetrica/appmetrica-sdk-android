package io.appmetrica.analytics.coreutils.internal

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.Locale

@RunWith(Parameterized::class)
class StringExtensionReplaceFirstCharWithTitleCaseTest(
    val input: String,
    val expected: String
) : CommonTest() {

    companion object {

        @JvmStatic
        @Parameters(name = "{0} -> {1}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf("", ""),
            arrayOf("first", "First"),
            arrayOf("Second", "Second"),
            arrayOf("two words", "Two words"),
            arrayOf("Two words", "Two words"),
            arrayOf("Two Words", "Two Words"),
            arrayOf("two Words", "Two Words"),
            arrayOf(" word after space", " word after space"),
            arrayOf(",word after comma", ",word after comma")
        )
    }

    @Test
    fun `replaceFirstCharWithTitleCase with default locale`() {
        assertThat(input.replaceFirstCharWithTitleCase()).isEqualTo(expected)
    }

    @Test
    fun `replaceFirstCharWithTitleCase with US locale`() {
        assertThat(input.replaceFirstCharWithTitleCase(Locale.US)).isEqualTo(expected)
    }
}
