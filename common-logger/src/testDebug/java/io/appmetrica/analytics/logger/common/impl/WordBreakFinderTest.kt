package io.appmetrica.analytics.logger.common.impl

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class WordBreakFinderTest(
    private val input: String,
    private val startOffset: Int,
    private val endOffset: Int,
    private val expected: Int,
) : CommonTest() {

    @Test
    fun find() {
        assertThat(WordBreakFinder().find(input, startOffset, endOffset)).isEqualTo(expected)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "#{index}")
        fun data(): List<Array<Any>> = listOf(
            arrayOf("asdadasdas", 0, 10, -1),
            arrayOf("asdadasdas,", 0, 11, 10),
            arrayOf("asdadasda,s", 0, 11, 9),
            arrayOf("a,sdadasdas", 0, 11, 1),
            arrayOf(",asdadasdas", 0, 11, 0),
            arrayOf("asda,dasdas", 0, 11, 4),
            arrayOf("asda,dasdas", 2, 5, 4),
            arrayOf("asda,dasdas", 1, 3, -1),
            arrayOf("asda,dasdas", 8, 10, -1),
            arrayOf("asda,dasdas", 10, 1, -1),
            arrayOf("as,da,das da,s,", 0, 13, 12),
            arrayOf("as,da,das da,s,", 0, 5, 2),
            arrayOf("as,da,das da,s,", 6, 10, 9),
            arrayOf("asdadasda s", 0, 11, 9),
            arrayOf("asdadasda;s", 0, 11, 9),
            arrayOf("asdadasda\ns", 0, 11, 9),
        )
    }
}
