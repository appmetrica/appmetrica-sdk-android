package io.appmetrica.analytics.coreutils.internal.io

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class GzipUtilsTest(
    private val input: ByteArray?,
    private val expected: ByteArray?
) {

    companion object {

        private val filledArray = ByteArray(10) { it.toByte() }

        @Parameterized.Parameters
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(null, null),
            arrayOf(ByteArray(0), ByteArray(0)),
            arrayOf(filledArray, filledArray)
        )
    }

    @Test
    fun `unGzip gzipped bytes`() {
        val gzippedValue = GZIPUtils.gzipBytes(input)
        val result = GZIPUtils.unGzipBytes(gzippedValue)
        assertThat(result).isEqualTo(expected)
    }
}
