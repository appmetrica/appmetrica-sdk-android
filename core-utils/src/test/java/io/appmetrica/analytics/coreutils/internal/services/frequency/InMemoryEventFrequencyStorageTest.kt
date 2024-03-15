package io.appmetrica.analytics.coreutils.internal.services.frequency

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class InMemoryEventFrequencyStorageTest : CommonTest() {

    private val inMemoryEventFrequencyStorage: InMemoryEventFrequencyStorage by setUp {
        InMemoryEventFrequencyStorage()
    }

    private val key = "key"

    @Test
    fun `getWindowStart if missing`() {
        assertThat(inMemoryEventFrequencyStorage.getWindowStart(key)).isNull()
    }

    @Test
    fun `getWindowStart after put`() {
        val value = 100500L
        inMemoryEventFrequencyStorage.putWindowStart(key, value)
        assertThat(inMemoryEventFrequencyStorage.getWindowStart(key)).isEqualTo(value)
    }

    @Test
    fun `getWindowOccurrencesCount if missing`() {
        assertThat(inMemoryEventFrequencyStorage.getWindowOccurrencesCount(key)).isNull()
    }

    @Test
    fun `getWindowOccurrencesCount after put`() {
        val value = 10500
        inMemoryEventFrequencyStorage.putWindowOccurrencesCount(key, value)
        assertThat(inMemoryEventFrequencyStorage.getWindowOccurrencesCount(key)).isEqualTo(value)
    }
}
