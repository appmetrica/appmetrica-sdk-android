package io.appmetrica.analytics.coreutils.internal.buffering

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LastValueTaskBufferTest : CommonTest() {

    private val buffer = LastValueTaskBuffer<String>()

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
    fun `add replaces previous value`() {
        buffer.add("first")
        buffer.add("second")
        buffer.add("third")

        val result = buffer.getAndClear()

        assertThat(result).containsExactly("third")
    }

    @Test
    fun `isEmpty returns true after getAndClear`() {
        buffer.add("value")
        buffer.getAndClear()

        assertThat(buffer.isEmpty()).isTrue()
    }
}
