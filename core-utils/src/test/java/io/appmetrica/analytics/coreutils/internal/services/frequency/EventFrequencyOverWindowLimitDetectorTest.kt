package io.appmetrica.analytics.coreutils.internal.services.frequency

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class EventFrequencyOverWindowLimitDetectorTest : CommonTest() {

    private val storage: EventFrequencyStorage = mock()

    private val initialUpTime = TimeUnit.HOURS.toMillis(1)

    @get:Rule
    val timeProviderMockedConstructionRule = constructionRule<SystemTimeProvider> {
        on { uptimeMillis() } doReturn initialUpTime
    }
    private val timeProvider: TimeProvider by timeProviderMockedConstructionRule

    private val initialWindow = TimeUnit.MINUTES.toMillis(5)
    private val initialLimitPerWindow = 10

    private val key = "key"
    private val initialCount = 4

    private val detector: EventFrequencyOverWindowLimitDetector by setUp {
        EventFrequencyOverWindowLimitDetector(initialWindow, initialLimitPerWindow, storage)
    }

    @Test
    fun `detect if no value for key`() {
        whenever(storage.getWindowStart(key)).thenReturn(null)
        assertThat(detector.detect(key)).isFalse()
        verify(storage).putWindowStart(key, initialUpTime)
        verify(storage).putWindowOccurrencesCount(key, 1)
    }

    @Test
    fun `detect if last window greater than uptime and count up to limit`() {
        whenever(storage.getWindowStart(key)).thenReturn(initialUpTime + 1)
        whenever(storage.getWindowOccurrencesCount(key)).thenReturn(initialCount)
        assertThat(detector.detect(key)).isFalse()
        verify(storage).putWindowStart(key, initialUpTime)
        verify(storage).putWindowOccurrencesCount(key, 1)
    }

    @Test
    fun `detect if last window greater than uptime and count over limit`() {
        whenever(storage.getWindowStart(key)).thenReturn(initialUpTime + 1)
        whenever(storage.getWindowOccurrencesCount(key)).thenReturn(initialLimitPerWindow + 1)
        assertThat(detector.detect(key)).isFalse()
        verify(storage).putWindowStart(key, initialUpTime)
        verify(storage).putWindowOccurrencesCount(key, 1)
    }

    @Test
    fun `detect if time in window and count up to limit`() {
        whenever(storage.getWindowStart(key)).thenReturn(initialUpTime - initialWindow + 1)
        whenever(storage.getWindowOccurrencesCount(key)).thenReturn(initialLimitPerWindow - 1)
        assertThat(detector.detect(key)).isFalse()
        verify(storage, never()).putWindowStart(eq(key), any())
        verify(storage).putWindowOccurrencesCount(key, initialLimitPerWindow)
    }

    @Test
    fun `detect if time in window and count over limit`() {
        whenever(storage.getWindowStart(key)).thenReturn(initialUpTime - initialWindow + 1)
        whenever(storage.getWindowOccurrencesCount(key)).thenReturn(initialLimitPerWindow)
        assertThat(detector.detect(key)).isTrue()
        verify(storage, never()).putWindowStart(eq(key), any())
        verify(storage).putWindowOccurrencesCount(key, initialLimitPerWindow + 1)
    }

    @Test
    fun `detect if time out of window and count up to limit`() {
        whenever(storage.getWindowStart(key)).thenReturn(initialUpTime - initialWindow - 1)
        whenever(storage.getWindowOccurrencesCount(key)).thenReturn(initialLimitPerWindow - 1)
        assertThat(detector.detect(key)).isFalse()
        verify(storage).putWindowStart(key, initialUpTime)
        verify(storage).putWindowOccurrencesCount(key, 1)
    }

    @Test
    fun `detect if time out of window and count over limit`() {
        whenever(storage.getWindowStart(key)).thenReturn(initialUpTime - initialWindow - 1)
        whenever(storage.getWindowOccurrencesCount(key)).thenReturn(initialLimitPerWindow)
        assertThat(detector.detect(key)).isFalse()
        verify(storage).putWindowStart(key, initialUpTime)
        verify(storage).putWindowOccurrencesCount(key, 1)
    }

    @Test
    fun `detect if time in window and count over limit after update time window`() {
        whenever(storage.getWindowStart(key)).thenReturn(initialUpTime - initialWindow + 1)
        whenever(storage.getWindowOccurrencesCount(key)).thenReturn(initialLimitPerWindow)
        detector.updateParameters(initialWindow - 10, initialLimitPerWindow)
        assertThat(detector.detect(key)).isFalse()
        verify(storage).putWindowStart(key, initialUpTime)
        verify(storage).putWindowOccurrencesCount(key, 1)
    }

    @Test
    fun `detect if time in window and count over limit after update event limit`() {
        whenever(storage.getWindowStart(key)).thenReturn(initialUpTime - initialWindow + 1)
        whenever(storage.getWindowOccurrencesCount(key)).thenReturn(initialLimitPerWindow)
        detector.updateParameters(initialWindow, initialLimitPerWindow + 1)
        assertThat(detector.detect(key)).isFalse()
        verify(storage, never()).putWindowStart(key, initialUpTime)
        verify(storage).putWindowOccurrencesCount(key, initialLimitPerWindow + 1)
    }
}
