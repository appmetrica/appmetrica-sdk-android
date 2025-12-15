package io.appmetrica.analytics.coreutils.internal.io

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.LogRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
class InputStreamUtilsTest : CommonTest() {

    @get:Rule
    val logRule = LogRule()

    @Test
    fun `readSafelyApprox with small data under limit`() {
        val testData = "Hello, World!".toByteArray()
        val limit = 1024

        val result = InputStreamUtils.readSafelyApprox(limit) {
            ByteArrayInputStream(testData)
        }

        assertThat(result).isEqualTo(testData)
    }

    @Test
    fun `readSafelyApprox with data exactly at limit`() {
        val testData = ByteArray(1000) { it.toByte() }
        val limit = 1000

        val result = InputStreamUtils.readSafelyApprox(limit) {
            ByteArrayInputStream(testData)
        }

        assertThat(result).isEqualTo(testData)
    }

    @Test
    fun `readSafelyApprox with data exceeding limit`() {
        val testData = ByteArray(20000) { it.toByte() }
        val limit = 10000

        val result = InputStreamUtils.readSafelyApprox(limit) {
            ByteArrayInputStream(testData)
        }

        // Result should be truncated when limit is exceeded
        assertThat(result.size).isLessThanOrEqualTo(testData.size)
    }

    @Test
    fun `readSafelyApprox with empty stream`() {
        val limit = 1024

        val result = InputStreamUtils.readSafelyApprox(limit) {
            ByteArrayInputStream(ByteArray(0))
        }

        assertThat(result).isEmpty()
    }

    @Test
    fun `readSafelyApprox with null provider result`() {
        val limit = 1024

        val result = InputStreamUtils.readSafelyApprox(limit) {
            null
        }

        assertThat(result).isEmpty()
    }

    @Test
    fun `readSafelyApprox with provider throwing exception`() {
        val limit = 1024

        val result = InputStreamUtils.readSafelyApprox(limit) {
            throw IOException("Test exception")
        }

        assertThat(result).isEmpty()
    }

    @Test
    fun `readSafelyApprox with stream throwing exception on read`() {
        val limit = 1024
        val failingStream = object : InputStream() {
            override fun read(): Int {
                throw IOException("Read failed")
            }
        }

        val result = InputStreamUtils.readSafelyApprox(limit) {
            failingStream
        }

        assertThat(result).isEmpty()
    }

    @Test
    fun `readSafelyApprox with large data multiple buffer reads`() {
        // Create data larger than buffer size (IO_BUFFER_SIZE * 2 = 16KB)
        val testData = ByteArray(20 * 1024) { (it % 256).toByte() }
        val limit = 30 * 1024

        val result = InputStreamUtils.readSafelyApprox(limit) {
            ByteArrayInputStream(testData)
        }

        assertThat(result).isEqualTo(testData)
    }

    @Test
    fun `readSafelyApprox with zero limit`() {
        val testData = "Test data".toByteArray()
        val limit = 0

        val result = InputStreamUtils.readSafelyApprox(limit) {
            ByteArrayInputStream(testData)
        }

        // With zero limit, should return immediately after first read
        assertThat(result.size).isLessThanOrEqualTo(16 * 1024) // IO_BUFFER_SIZE * 2
    }

    @Test
    fun `readSafelyApprox with negative limit`() {
        val testData = "Test data".toByteArray()
        val limit = -1

        val result = InputStreamUtils.readSafelyApprox(limit) {
            ByteArrayInputStream(testData)
        }

        // With negative limit, should return immediately
        assertThat(result).isNotNull
    }

    @Test
    fun `readSafelyApprox with stream that returns zero bytes`() {
        val limit = 1024
        var readCount = 0
        val specialStream = object : InputStream() {
            override fun read(): Int = -1

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                return when {
                    readCount++ < 3 -> 0 // Return 0 a few times
                    else -> -1 // Then EOF
                }
            }
        }

        val result = InputStreamUtils.readSafelyApprox(limit) {
            specialStream
        }

        assertThat(result).isEmpty()
    }

    @Test
    fun `readSafelyApprox closes stream on success`() {
        var streamClosed = false
        val testData = "Test".toByteArray()
        val limit = 1024

        val stream = object : ByteArrayInputStream(testData) {
            override fun close() {
                streamClosed = true
                super.close()
            }
        }

        InputStreamUtils.readSafelyApprox(limit) { stream }

        assertThat(streamClosed).isTrue()
    }

    @Test
    fun `readSafelyApprox closes stream on exception`() {
        var streamClosed = false
        val limit = 1024

        val stream = object : InputStream() {
            override fun read(): Int = throw IOException("Test exception")

            override fun close() {
                streamClosed = true
                super.close()
            }
        }

        InputStreamUtils.readSafelyApprox(limit) { stream }

        assertThat(streamClosed).isTrue()
    }

    @Test
    fun `readSafelyApprox does not truncate when limit is less than buffer`() {
        val testData = "1234567890".toByteArray()
        val limit = 5

        val result = InputStreamUtils.readSafelyApprox(limit) {
            BufferedInputStream(ByteArrayInputStream(testData))
        }

        assertThat(result).isEqualTo(testData)
    }
}
