package io.appmetrica.analytics.coreutils.internal.buffering

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MapMergingBufferTest : CommonTest() {

    private val buffer = MapMergingBuffer<String, Int>()

    @Test
    fun `isEmpty returns true when no values added`() {
        assertThat(buffer.isEmpty()).isTrue()
    }

    @Test
    fun `isEmpty returns false after adding map`() {
        buffer.add(mapOf("key" to 1))
        assertThat(buffer.isEmpty()).isFalse()
    }

    @Test
    fun `getAndClear returns empty list when no values added`() {
        assertThat(buffer.getAndClear()).isEmpty()
    }

    @Test
    fun `getAndClear returns single merged map`() {
        buffer.add(mapOf("key1" to 1, "key2" to 2))

        val result = buffer.getAndClear()

        assertThat(result).hasSize(1)
        assertThat(result[0]).containsEntry("key1", 1)
        assertThat(result[0]).containsEntry("key2", 2)
    }

    @Test
    fun `getAndClear clears the buffer`() {
        buffer.add(mapOf("key" to 1))
        buffer.getAndClear()

        assertThat(buffer.isEmpty()).isTrue()
        assertThat(buffer.getAndClear()).isEmpty()
    }

    @Test
    fun `add merges maps with later values overwriting earlier ones`() {
        buffer.add(mapOf("key1" to 1, "key2" to 2))
        buffer.add(mapOf("key2" to 20, "key3" to 3))
        buffer.add(mapOf("key1" to 10))

        val result = buffer.getAndClear()

        assertThat(result).hasSize(1)
        assertThat(result[0]).containsEntry("key1", 10)
        assertThat(result[0]).containsEntry("key2", 20)
        assertThat(result[0]).containsEntry("key3", 3)
    }

    @Test
    fun `isEmpty returns true after getAndClear`() {
        buffer.add(mapOf("key1" to 1, "key2" to 2))
        buffer.getAndClear()

        assertThat(buffer.isEmpty()).isTrue()
    }

    @Test
    fun `multiple getAndClear cycles work correctly`() {
        buffer.add(mapOf("a" to 1, "b" to 2))
        val first = buffer.getAndClear()
        assertThat(first[0]).containsEntry("a", 1)
        assertThat(first[0]).containsEntry("b", 2)

        buffer.add(mapOf("c" to 3))
        val second = buffer.getAndClear()
        assertThat(second[0]).containsEntry("c", 3)
        assertThat(second[0]).hasSize(1)

        assertThat(buffer.getAndClear()).isEmpty()
    }

    @Test
    fun `adding empty map does not affect isEmpty`() {
        buffer.add(emptyMap())
        assertThat(buffer.isEmpty()).isTrue()
    }
}
