package io.appmetrica.analytics.coreutils.internal.buffering

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AccumulatingTaskBufferTest : CommonTest() {

    private val buffer = AccumulatingTaskBuffer<String>()

    @Test
    fun `isEmpty returns true when no values added`() {
        assertThat(buffer.isEmpty()).isTrue()
    }

    @Test
    fun `isEmpty returns false after adding value`() {
        buffer.add("value")
        assertThat(buffer.isEmpty()).isFalse()
    }

    @Test
    fun `getAndClear returns empty list when no values added`() {
        assertThat(buffer.getAndClear()).isEmpty()
    }

    @Test
    fun `getAndClear returns single value`() {
        buffer.add("test")

        val result = buffer.getAndClear()

        assertThat(result).containsExactly("test")
    }

    @Test
    fun `getAndClear clears the buffer`() {
        buffer.add("test")
        buffer.getAndClear()

        assertThat(buffer.isEmpty()).isTrue()
        assertThat(buffer.getAndClear()).isEmpty()
    }

    @Test
    fun `add accumulates all values`() {
        buffer.add("first")
        buffer.add("second")
        buffer.add("third")

        val result = buffer.getAndClear()

        assertThat(result).containsExactly("first", "second", "third")
    }

    @Test
    fun `isEmpty returns true after getAndClear`() {
        buffer.add("value1")
        buffer.add("value2")
        buffer.getAndClear()

        assertThat(buffer.isEmpty()).isTrue()
    }

    @Test
    fun `multiple getAndClear cycles work correctly`() {
        buffer.add("a")
        buffer.add("b")
        assertThat(buffer.getAndClear()).containsExactly("a", "b")

        buffer.add("c")
        assertThat(buffer.getAndClear()).containsExactly("c")

        assertThat(buffer.getAndClear()).isEmpty()
    }
}
