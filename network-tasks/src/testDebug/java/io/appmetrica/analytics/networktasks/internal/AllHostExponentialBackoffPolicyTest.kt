package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.networktasks.internal.attempt.AttemptFailureReason
import io.appmetrica.analytics.networktasks.internal.attempt.AttemptOutcome
import io.appmetrica.analytics.networktasks.internal.attempt.RetryState
import io.appmetrica.analytics.networktasks.internal.backoff.PreserveBackoffOnPayloadRejected
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class AllHostExponentialBackoffPolicyTest : CommonTest() {

    private val dataHolder = mock<ExponentialBackoffDataHolder>()
    private val retryPolicyConfig = mock<RetryPolicyConfig>()
    private val policy = AllHostsExponentialBackoffPolicy(dataHolder)

    @Test
    fun onHostAttemptFinishedDoesNothing() {
        policy.onHostAttemptFinished(succeeded())
        policy.onHostAttemptFinished(failed(AttemptFailureReason.UNCLASSIFIED))
        policy.onHostAttemptFinished(failed(AttemptFailureReason.PAYLOAD_REJECTED))
        verifyNoMoreInteractions(dataHolder)
    }

    @Test
    fun onAllHostsAttemptsFinishedAppliesRules() {
        policy.onAllHostsAttemptsFinished(succeeded())
        verify(dataHolder).reset()

        clearInvocations(dataHolder)
        policy.onAllHostsAttemptsFinished(failed(AttemptFailureReason.UNCLASSIFIED))
        verify(dataHolder).updateLastAttemptInfo()

        clearInvocations(dataHolder)
        val preservePolicy = AllHostsExponentialBackoffPolicy(dataHolder, PreserveBackoffOnPayloadRejected)
        preservePolicy.onAllHostsAttemptsFinished(failed(AttemptFailureReason.PAYLOAD_REJECTED))
        verifyNoMoreInteractions(dataHolder)
    }

    @Test
    fun onAllHostsAttemptsFinishedForSkippedDoesNothing() {
        policy.onAllHostsAttemptsFinished(AttemptOutcome.Skipped)
        policy.onAllHostsAttemptsFinished(AttemptOutcome.ReadyToRun)
        verifyNoMoreInteractions(dataHolder)
    }

    @Test
    fun canBeExecutedForTrue() = canBeExecuted(true)

    @Test
    fun canBeExecutedForFalse() = canBeExecuted(false)

    private fun canBeExecuted(result: Boolean) {
        whenever(dataHolder.wasLastAttemptLongAgoEnough(retryPolicyConfig)).thenReturn(result)
        assertThat(policy.canBeExecuted(retryPolicyConfig)).isEqualTo(result)
    }

    private fun succeeded() = AttemptOutcome.Completed(
        succeeded = true,
        retry = RetryState.STOP,
        failureReason = AttemptFailureReason.UNCLASSIFIED,
    )

    private fun failed(reason: AttemptFailureReason) = AttemptOutcome.Completed(
        succeeded = false,
        retry = RetryState.STOP,
        failureReason = reason,
    )
}
