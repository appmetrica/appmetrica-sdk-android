package io.appmetrica.analytics.coreutils.internal.time

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

private const val TAG = "tag"

@RunWith(RobolectricTestRunner::class)
class TimePassedCheckerTest {

    private val currentTimeSeconds = 12345678L
    private val currentTimeMillis = 87654321L
    private val timeProvider = mock<TimeProvider> {
        on { this.currentTimeMillis() } doReturn currentTimeMillis
        on { this.currentTimeSeconds() } doReturn currentTimeSeconds
    }
    private val timePassedChecker = TimePassedChecker(timeProvider)

    @Test
    fun testCurrentLessThanLastSeconds() {
        assertThat(timePassedChecker.didTimePassSeconds(currentTimeSeconds + 1, 0, TAG)).isTrue
    }

    @Test
    fun testIntervalPassedSeconds() {
        assertThat(timePassedChecker.didTimePassSeconds(currentTimeSeconds - 11, 10, TAG)).isTrue
    }

    @Test
    fun testIntervalNotPassedSeconds() {
        assertThat(timePassedChecker.didTimePassSeconds(currentTimeSeconds - 9, 10, TAG)).isFalse
    }

    @Test
    fun testIntervalPassedExactlySeconds() {
        assertThat(timePassedChecker.didTimePassSeconds(currentTimeSeconds - 10, 10, TAG)).isTrue
    }

    @Test
    fun testCurrentLessThanLastMillis() {
        assertThat(timePassedChecker.didTimePassMillis(currentTimeMillis + 1, 0, TAG)).isTrue
    }

    @Test
    fun testIntervalPassedMillis() {
        assertThat(timePassedChecker.didTimePassMillis(currentTimeMillis - 11, 10, TAG)).isTrue
    }

    @Test
    fun testIntervalNotPassedMillis() {
        assertThat(timePassedChecker.didTimePassMillis(currentTimeMillis - 9, 10, TAG)).isFalse
    }

    @Test
    fun testIntervalPassedExactlyMillis() {
        assertThat(timePassedChecker.didTimePassMillis(currentTimeMillis - 10, 10, TAG)).isTrue
    }
}
