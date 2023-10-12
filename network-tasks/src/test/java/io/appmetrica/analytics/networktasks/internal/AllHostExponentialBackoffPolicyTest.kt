package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class AllHostExponentialBackoffPolicyTest : CommonTest() {

    private val dataHolder = mock<ExponentialBackoffDataHolder>()
    private val retryPolicyConfig = mock<RetryPolicyConfig>()
    private val policy = AllHostsExponentialBackoffPolicy(dataHolder)

    @Test
    fun onHostAttemptFinishedForTrue() = onHostAttemptFinished(true)

    @Test
    fun onHostAttemptFinishedForFalse() = onHostAttemptFinished(false)

    private fun onHostAttemptFinished(success: Boolean) {
        policy.onHostAttemptFinished(success)
        verifyNoMoreInteractions(dataHolder)
    }

    @Test
    fun onAllHostAttemptsFinishedForTrue() {
        policy.onAllHostsAttemptsFinished(true)
        verify(dataHolder).reset()
    }

    @Test
    fun onAllAttemptsFinishedForFalse() {
        policy.onAllHostsAttemptsFinished(false)
        verify(dataHolder).updateLastAttemptInfo()
    }

    @Test
    fun canBeExecutedForTrue() = canBeExecuted(true)

    @Test
    fun canBeExecutedForFalse() = canBeExecuted(false)

    private fun canBeExecuted(result: Boolean) {
        whenever(dataHolder.wasLastAttemptLongAgoEnough(retryPolicyConfig)).thenReturn(result)
        assertThat(policy.canBeExecuted(retryPolicyConfig)).isEqualTo(result)
    }
}
