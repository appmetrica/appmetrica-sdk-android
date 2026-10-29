package io.appmetrica.analytics.networktasks.impl

import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.networktasks.internal.AllHostsExponentialBackoffPolicy
import io.appmetrica.analytics.networktasks.internal.ExponentialBackoffDataHolder
import io.appmetrica.analytics.networktasks.internal.HostRetryInfoProvider
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig
import io.appmetrica.analytics.networktasks.internal.attempt.AttemptFailureReason
import io.appmetrica.analytics.networktasks.internal.attempt.AttemptOutcome
import io.appmetrica.analytics.networktasks.internal.attempt.RetryState
import io.appmetrica.analytics.networktasks.internal.backoff.PreserveBackoffOnPayloadRejected
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * Two reporters share one REPORT backoff holder:
 * a handled HTTP 400 for reporter A must not introduce delay for reporter B.
 */
internal class SharedBackoffBadRequestTest : CommonTest() {

    private val retryPolicyConfig = RetryPolicyConfig(500, 3)
    private val retryInfoProvider = mock<HostRetryInfoProvider> {
        on { this.nextSendAttemptNumber } doReturn 1
        on { this.lastAttemptTimeSeconds } doReturn 0L
    }
    private val sharedHolder = ExponentialBackoffDataHolder(retryInfoProvider, "REPORT")
    private val policyA = AllHostsExponentialBackoffPolicy(sharedHolder, PreserveBackoffOnPayloadRejected)
    private val policyB = AllHostsExponentialBackoffPolicy(sharedHolder, PreserveBackoffOnPayloadRejected)

    private val connectionPolicy = mock<IExecutionPolicy> {
        on { this.canBeExecuted() } doReturn true
    }
    private val rootThread = mock<InterruptionSafeThread> {
        on { this.isRunning } doReturn true
    }

    @Test
    fun handledBadRequestFromTaskADoesNotDelayTaskB() {
        assertThat(policyB.canBeExecuted(retryPolicyConfig)).isTrue

        runTask(
            backoffPolicy = policyA,
            decision = AttemptOutcome.Completed(
                succeeded = false,
                retry = RetryState.STOP,
                failureReason = AttemptFailureReason.PAYLOAD_REJECTED,
            ),
        )

        assertThat(policyB.canBeExecuted(retryPolicyConfig)).isTrue
    }

    @Test
    fun transientFailureFromTaskADelaysTaskB() {
        assertThat(policyB.canBeExecuted(retryPolicyConfig)).isTrue

        runTask(
            backoffPolicy = policyA,
            decision = AttemptOutcome.Completed(
                succeeded = false,
                retry = RetryState.STOP,
                failureReason = AttemptFailureReason.UNCLASSIFIED,
            ),
        )

        assertThat(policyB.canBeExecuted(retryPolicyConfig)).isFalse
    }

    @Test
    fun skippedTaskDoesNotResetSharedBackoff() {
        runSkippedAgainstNonzeroHolder(usePreserveRules = true)
    }

    @Test
    fun skippedTaskDoesNotResetSharedBackoffWithDefaultRules() {
        runSkippedAgainstNonzeroHolder(usePreserveRules = false)
    }

    private fun runSkippedAgainstNonzeroHolder(usePreserveRules: Boolean) {
        // Non-zero attempt counter, but delay window already elapsed (timestamp == 0).
        val retryInfoProvider = mock<HostRetryInfoProvider> {
            on { this.nextSendAttemptNumber } doReturn 3
            on { this.lastAttemptTimeSeconds } doReturn 0L
        }
        val holder = ExponentialBackoffDataHolder(retryInfoProvider, "REPORT")
        val policy = if (usePreserveRules) {
            AllHostsExponentialBackoffPolicy(holder, PreserveBackoffOnPayloadRejected)
        } else {
            AllHostsExponentialBackoffPolicy(holder)
        }
        assertThat(policy.canBeExecuted(retryPolicyConfig)).isTrue

        val networkTask = mock<NetworkTask> {
            on { this.exponentialBackoffPolicy } doReturn policy
            on { this.connectionExecutionPolicy } doReturn connectionPolicy
            on { this.onCreateNetworkTask() } doReturn false
            on { this.retryPolicyConfig } doReturn retryPolicyConfig
        }
        NetworkTaskRunnable(networkTask, rootThread, mock()).run()

        verify(retryInfoProvider, never()).saveNextSendAttemptNumber(any())
        verify(retryInfoProvider, never()).saveLastAttemptTimeSeconds(any())
    }

    private fun runTask(
        backoffPolicy: AllHostsExponentialBackoffPolicy,
        decision: AttemptOutcome,
    ) {
        val networkTask = mock<NetworkTask> {
            on { this.exponentialBackoffPolicy } doReturn backoffPolicy
            on { this.connectionExecutionPolicy } doReturn connectionPolicy
            on { this.onCreateNetworkTask() } doReturn true
            on { this.retryPolicyConfig } doReturn retryPolicyConfig
        }
        val performingStrategy = mock<NetworkTaskPerformingStrategy> {
            on { this.performRequest(networkTask) } doReturn decision
        }
        NetworkTaskRunnable(networkTask, rootThread, performingStrategy).run()
    }
}
