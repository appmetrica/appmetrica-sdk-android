package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExponentialBackoffDataHolderTest {

    private val maxTimeoutSeconds = 500
    private val exponentialMultiplier = 3
    private val currentTimeSeconds = 123456789L
    private val retryPolicyConfig =
        RetryPolicyConfig(
            maxTimeoutSeconds,
            exponentialMultiplier
        )
    private val retryInfoProvider = mock<HostRetryInfoProvider> {
        on { this.nextSendAttemptNumber } doReturn 1
        on { this.lastAttemptTimeSeconds } doReturn currentTimeSeconds - maxTimeoutSeconds / 2
    }
    private val timePassedChecker = mock<TimePassedChecker>()
    private val timeProvider = mock<TimeProvider> {
        on { this.currentTimeSeconds() } doReturn currentTimeSeconds
    }
    private var exponentialBackoffDataHolder =
        ExponentialBackoffDataHolder(
            retryInfoProvider,
            timeProvider,
            timePassedChecker
        )

    @Test
    fun wasLastAttemptLongAgoEnoughNullConfig() {
        assertThat(exponentialBackoffDataHolder.wasLastAttemptLongAgoEnough(null)).isTrue
    }

    @Test
    fun wasLastAttemptLongAgoEnoughFirstAttempt() {
        stubbing(retryInfoProvider) {
            on { this.lastAttemptTimeSeconds } doReturn 0L
        }
        exponentialBackoffDataHolder =
            ExponentialBackoffDataHolder(
                retryInfoProvider,
                timeProvider,
                timePassedChecker
            )
        assertThat(exponentialBackoffDataHolder.wasLastAttemptLongAgoEnough(retryPolicyConfig)).isTrue
    }

    @Test
    fun wasLastAttemptLongAgoEnoughTimeDidNotPass() {
        assertThat(exponentialBackoffDataHolder.wasLastAttemptLongAgoEnough(retryPolicyConfig)).isFalse
    }

    @Test
    fun wasLastAttemptLongAgoEnoughTimePassed() {
        stubbing(timePassedChecker) {
            // 3 * (2^4 - 1) = 45
            on { this.didTimePassSeconds(any(), any(), any()) } doReturn true
        }
        assertThat(exponentialBackoffDataHolder.wasLastAttemptLongAgoEnough(retryPolicyConfig)).isTrue
    }

    @Test
    fun useCorrectArgumentsFromTimePassedChecker() {
        val lastAttemptSeconds = 834768762L
        stubbing(retryInfoProvider) {
            on { this.lastAttemptTimeSeconds } doReturn lastAttemptSeconds
            on { this.nextSendAttemptNumber } doReturn 5
        }
        exponentialBackoffDataHolder =
            ExponentialBackoffDataHolder(
                retryInfoProvider,
                timeProvider,
                timePassedChecker
            )
        exponentialBackoffDataHolder.wasLastAttemptLongAgoEnough(retryPolicyConfig)
        // 3 * (2^4 - 1) = 45
        verify(timePassedChecker).didTimePassSeconds(lastAttemptSeconds, 45, "last send attempt")
    }

    @Test
    fun useMaxIntervalForTimePassedChecker() {
        val lastAttemptSeconds = 834768762L
        stubbing(retryInfoProvider) {
            on { this.lastAttemptTimeSeconds } doReturn lastAttemptSeconds
            on { this.nextSendAttemptNumber } doReturn 9
        }
        exponentialBackoffDataHolder =
            ExponentialBackoffDataHolder(
                retryInfoProvider,
                timeProvider,
                timePassedChecker
            )
        exponentialBackoffDataHolder.wasLastAttemptLongAgoEnough(retryPolicyConfig)
        // 3 * (2^8 - 1) = 765 > 500
        verify(timePassedChecker).didTimePassSeconds(lastAttemptSeconds, 500, "last send attempt")
    }

    @Test
    fun saveLastAttemptInfo() {
        stubbing(retryInfoProvider) {
            on { this.lastAttemptTimeSeconds } doReturn 0L
        }
        exponentialBackoffDataHolder =
            ExponentialBackoffDataHolder(
                retryInfoProvider,
                timeProvider,
                timePassedChecker
            )
        exponentialBackoffDataHolder.updateLastAttemptInfo()
        verify(retryInfoProvider).saveLastAttemptTimeSeconds(currentTimeSeconds)
        verify(retryInfoProvider).saveNextSendAttemptNumber(2)
        assertThat(exponentialBackoffDataHolder.wasLastAttemptLongAgoEnough(retryPolicyConfig)).isFalse
    }

    @Test
    fun reset() {
        stubbing(retryInfoProvider) {
            on { this.lastAttemptTimeSeconds } doReturn 0L
        }
        exponentialBackoffDataHolder =
            ExponentialBackoffDataHolder(
                retryInfoProvider,
                timeProvider,
                timePassedChecker
            )
        exponentialBackoffDataHolder.updateLastAttemptInfo()
        exponentialBackoffDataHolder.reset()
        verify(retryInfoProvider).saveLastAttemptTimeSeconds(0)
        verify(retryInfoProvider).saveNextSendAttemptNumber(1)
        assertThat(exponentialBackoffDataHolder.wasLastAttemptLongAgoEnough(retryPolicyConfig)).isTrue
    }
}
