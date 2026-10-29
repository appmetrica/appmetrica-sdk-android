package io.appmetrica.analytics.networktasks.impl

import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.networktasks.internal.ExponentialBackoffPolicy
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig
import io.appmetrica.analytics.networktasks.internal.attempt.AttemptFailureReason
import io.appmetrica.analytics.networktasks.internal.attempt.AttemptOutcome
import io.appmetrica.analytics.networktasks.internal.attempt.RetryState
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class NetworkTaskRunnableTest : CommonTest() {

    private val retryPolicyConfig = mock<RetryPolicyConfig>()
    private val connectionBasedExecutionPolicy = mock<IExecutionPolicy> {
        on { this.canBeExecuted() } doReturn true
    }
    private val exponentialBackoffPolicy = mock<ExponentialBackoffPolicy> {
        on { this.canBeExecuted(retryPolicyConfig) } doReturn true
    }
    private val networkTask = mock<NetworkTask> {
        on { this.exponentialBackoffPolicy } doReturn exponentialBackoffPolicy
        on { this.connectionExecutionPolicy } doReturn connectionBasedExecutionPolicy
        on { this.onCreateNetworkTask() } doReturn true
        on { this.retryPolicyConfig } doReturn retryPolicyConfig
    }
    private val performingStrategy = mock<NetworkTaskPerformingStrategy>()
    private val rootThreadStateSource = mock<InterruptionSafeThread> {
        on { this.isRunning } doReturn true
    }
    private val networkTaskRunnable = NetworkTaskRunnable(
        networkTask,
        rootThreadStateSource,
        performingStrategy
    )

    @Test
    fun cannotBeExecuted() {
        stubbing(connectionBasedExecutionPolicy) {
            on { this.canBeExecuted() } doReturn false
        }
        stubbing(exponentialBackoffPolicy) {
            on { this.canBeExecuted(retryPolicyConfig) } doReturn false
        }
        networkTaskRunnable.run()
        verify(networkTask).onShouldNotExecute()
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(any())
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(any())
    }

    @Test
    fun networkPolicyForbidsExecution() {
        stubbing(connectionBasedExecutionPolicy) {
            on { this.canBeExecuted() } doReturn false
        }
        networkTaskRunnable.run()
        verify(networkTask).onShouldNotExecute()
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(any())
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(any())
    }

    @Test
    fun retryPolicyForbidsExecution() {
        stubbing(exponentialBackoffPolicy) {
            on { this.canBeExecuted(retryPolicyConfig) } doReturn false
        }
        networkTaskRunnable.run()
        verify(networkTask).onShouldNotExecute()
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(any())
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(any())
    }

    @Test
    fun networkTaskNotCreatedDoesNotUpdateBackoff() {
        stubbing(networkTask) {
            on { this.onCreateNetworkTask() } doReturn false
        }
        networkTaskRunnable.run()
        verify(performingStrategy, never()).performRequest(networkTask)
        verify(networkTask, never()).onShouldNotExecute()
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(any())
        verify(exponentialBackoffPolicy).onAllHostsAttemptsFinished(AttemptOutcome.Skipped)
    }

    @Test
    fun requestUnsuccessfulShouldNotTryNextHost() {
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturn failedDecision(retry = RetryState.STOP)
        }
        networkTaskRunnable.run()
        verify(exponentialBackoffPolicy, times(1)).onHostAttemptFinished(completedFailure(RetryState.STOP))
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(completedFailure(RetryState.STOP))
        verify(performingStrategy).performRequest(networkTask)
    }

    @Test
    fun requestUnsuccessfulShouldTryNextHost() {
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturnConsecutively listOf(
                failedDecision(retry = RetryState.RUN),
                failedDecision(retry = RetryState.STOP),
            )
        }
        networkTaskRunnable.run()
        verify(exponentialBackoffPolicy, times(2)).onHostAttemptFinished(
            check { assertCompletedFailure(it) }
        )
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(
            completedFailure(RetryState.STOP)
        )
        verify(performingStrategy, times(2)).performRequest(networkTask)
    }

    @Test
    fun requestSuccessful() {
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturn successfulDecision()
        }
        networkTaskRunnable.run()
        verify(exponentialBackoffPolicy, times(1)).onHostAttemptFinished(successfulDecision())
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(successfulDecision())
        verify(performingStrategy, times(1)).performRequest(networkTask)
    }

    @Test
    fun handledFailureDoesNotUpdateBackoff() {
        val decision = AttemptOutcome.Completed(
            succeeded = false,
            retry = RetryState.STOP,
            failureReason = AttemptFailureReason.PAYLOAD_REJECTED,
        )
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturn decision
        }
        networkTaskRunnable.run()
        verify(exponentialBackoffPolicy, times(1)).onHostAttemptFinished(decision)
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(decision)
        verify(performingStrategy, times(1)).performRequest(networkTask)
    }

    @Test
    fun requestSuccessfulShouldTryNextHost() {
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturn successfulDecision()
        }
        networkTaskRunnable.run()
        verify(exponentialBackoffPolicy, times(1)).onHostAttemptFinished(successfulDecision())
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(successfulDecision())
        verify(performingStrategy, times(1)).performRequest(networkTask)
    }

    @Test
    fun threadIsNotRunning() {
        stubbing(rootThreadStateSource) {
            on { this.isRunning } doReturn false
        }
        networkTaskRunnable.run()
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(any())
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(any())
    }

    @Test
    fun threadIsInterrupted() {
        stubbing(connectionBasedExecutionPolicy) {
            on { this.canBeExecuted() } doReturn true
        }
        stubbing(networkTask) {
            on { this.onCreateNetworkTask() } doReturn true
        }
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturn failedDecision(retry = RetryState.RUN)
        }
        stubbing(rootThreadStateSource) {
            on { this.isRunning } doReturnConsecutively listOf(true, true, true, true, true, false)
        }
        networkTaskRunnable.run()
        verify(performingStrategy, times(4)).performRequest(networkTask)
        verify(exponentialBackoffPolicy, times(4)).onHostAttemptFinished(
            check { assertCompletedFailure(it, RetryState.RUN) }
        )
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(
            check { assertCompletedFailure(it, RetryState.RUN) }
        )
    }

    @Test
    fun threadIsNotRunningWhenPerformingRequest() {
        stubbing(rootThreadStateSource) {
            on { this.isRunning } doReturnConsecutively listOf(true, false)
        }
        networkTaskRunnable.run()
        verify(performingStrategy, never()).performRequest(networkTask)
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(any())
        verify(exponentialBackoffPolicy).onAllHostsAttemptsFinished(AttemptOutcome.ReadyToRun)
    }

    @Test
    fun threadIsInterruptedWhilePerformingRequest() {
        stubbing(rootThreadStateSource) {
            on { this.isRunning } doReturnConsecutively listOf(true, true, true, true, false)
        }
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturn failedDecision(retry = RetryState.RUN)
        }
        networkTaskRunnable.run()
        verify(performingStrategy, times(3)).performRequest(networkTask)
        verify(exponentialBackoffPolicy, times(3)).onHostAttemptFinished(
            check { assertCompletedFailure(it, RetryState.RUN) }
        )
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(
            check { assertCompletedFailure(it, RetryState.RUN) }
        )
    }

    @Test
    fun shouldNotExecuteTaskStopsExecuting() {
        stubbing(rootThreadStateSource) {
            on { this.isRunning } doReturn true
        }
        stubbing(connectionBasedExecutionPolicy) {
            on { this.canBeExecuted() } doReturn true
        }
        stubbing(networkTask) {
            on { this.onCreateNetworkTask() } doReturn true
        }
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturn failedDecision(retry = RetryState.STOP)
        }
        networkTaskRunnable.run()
        verify(networkTask, times(1)).onCreateNetworkTask()
    }

    private fun successfulDecision() = AttemptOutcome.Completed(
        succeeded = true,
        retry = RetryState.STOP,
        failureReason = AttemptFailureReason.UNCLASSIFIED,
    )

    private fun failedDecision(retry: RetryState) = AttemptOutcome.Completed(
        succeeded = false,
        retry = retry,
        failureReason = AttemptFailureReason.UNCLASSIFIED,
    )

    private fun completedFailure(retry: RetryState) = failedDecision(retry)

    private fun assertCompletedFailure(
        outcome: AttemptOutcome,
        retry: RetryState? = null,
    ) {
        assertThat(outcome).isInstanceOf(AttemptOutcome.Completed::class.java)
        val completed = outcome as AttemptOutcome.Completed
        assertThat(completed.succeeded).isFalse
        assertThat(completed.failureReason).isEqualTo(AttemptFailureReason.UNCLASSIFIED)
        if (retry != null) {
            assertThat(completed.retry).isEqualTo(retry)
        }
    }
}
