package io.appmetrica.analytics.network.impl.utils

import io.appmetrica.analytics.network.impl.utils.Utils.unmodifiableMapCopy
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream

class UtilsTest : CommonTest() {

    @Test
    fun readSafelySuccess() {
        val response = "some response".toByteArray()
        val inputStream = BufferedInputStream(ByteArrayInputStream(response))
        assertThat(Utils.readSafely(Int.MAX_VALUE) { inputStream }).isEqualTo(response)
        assertThat(inputStream)
    }

    @Test
    fun readSafelyException() {
        val inputStream = mock<InputStream> {
            on { this.read(any(), any(), any()) } doThrow RuntimeException()
            on { this.read(any()) } doThrow RuntimeException()
        }
        assertThat(Utils.readSafely(Int.MAX_VALUE) { inputStream }).isEqualTo(ByteArray(0))
        verify(inputStream).close()
    }

    @Test
    fun readSafelyTruncates() {
        val response = "a".repeat(8200).toByteArray()
        val inputStream = BufferedInputStream(ByteArrayInputStream(response))
        assertThat(Utils.readSafely(8100) { inputStream }).isEqualTo("a".repeat(8192).toByteArray())
        assertThat(inputStream)
    }

    @Test
    fun readSafelyDoesNotTruncateWhenLimitIsLessThanBuffer() {
        val response = "1234567890".toByteArray()
        val inputStream = BufferedInputStream(ByteArrayInputStream(response))
        assertThat(Utils.readSafely(5) { inputStream }).isEqualTo(response)
        assertThat(inputStream)
    }

    @Test
    fun unmodifiableMapCopyDoesNotNoticeChangesToOriginalCollection() {
        val original = mutableMapOf("key1" to "value1", "key2" to "value2")
        val originalCopy = mutableMapOf("key1" to "value1", "key2" to "value2")
        val mapCopy = unmodifiableMapCopy(original)
        assertThat(mapCopy).containsExactlyInAnyOrderEntriesOf(originalCopy)
        original["key3"] = "value3"
        assertThat(mapCopy).containsExactlyInAnyOrderEntriesOf(originalCopy)
    }
}
