package io.appmetrica.analytics.networktasks.impl

import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.networktasks.internal.ExponentialBackoffPolicy
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkTaskRunnableTest {

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
        on { this.shouldTryNextHost() } doReturn false
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
    fun testCannotBeExecuted() {
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
    fun testNetworkPolicyForbidsExecution() {
        stubbing(connectionBasedExecutionPolicy) {
            on { this.canBeExecuted() } doReturn false
        }
        networkTaskRunnable.run()
        verify(networkTask).onShouldNotExecute()
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(any())
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(any())
    }

    @Test
    fun testRetryPolicyForbidsExecution() {
        stubbing(exponentialBackoffPolicy) {
            on { this.canBeExecuted(retryPolicyConfig) } doReturn false
        }
        networkTaskRunnable.run()
        verify(networkTask).onShouldNotExecute()
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(any())
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(any())
    }

    @Test
    fun testShouldNotExecute() {
        stubbing(networkTask) {
            on { this.onCreateNetworkTask() } doReturn false
        }
        networkTaskRunnable.run()
        verify(performingStrategy, never()).performRequest(networkTask)
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(any())
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(any())
    }

    @Test
    fun testRequestUnsuccessfulShouldNotTryNextHost() {
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturn false
        }
        stubbing(networkTask) {
            on { this.shouldTryNextHost() } doReturn false
        }
        networkTaskRunnable.run()
        verify(exponentialBackoffPolicy, times(1)).onHostAttemptFinished(false)
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(true)
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(false)
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(true)
        verify(performingStrategy).performRequest(networkTask)
    }

    @Test
    fun testRequestUnsuccessfulShouldTryNextHost() {
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturn false
        }
        stubbing(networkTask) {
            on { this.shouldTryNextHost() } doReturnConsecutively listOf(true, false)
        }
        networkTaskRunnable.run()
        verify(exponentialBackoffPolicy, times(2)).onHostAttemptFinished(false)
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(true)
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(false)
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(true)
        verify(performingStrategy, times(2)).performRequest(networkTask)
    }

    @Test
    fun testRequestSuccessful() {
        stubbing(networkTask) {
            on { this.shouldTryNextHost() } doReturn false
        }
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturn true
        }
        networkTaskRunnable.run()
        verify(exponentialBackoffPolicy, times(1)).onHostAttemptFinished(true)
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(false)
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(true)
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(false)
        verify(performingStrategy, times(1)).performRequest(networkTask)
    }

    @Test
    fun testRequestSuccessfulShouldTryNextHost() {
        stubbing(networkTask) {
            on { this.shouldTryNextHost() } doReturn true
        }
        stubbing(performingStrategy) {
            on { this.performRequest(networkTask) } doReturn true
        }
        networkTaskRunnable.run()
        verify(exponentialBackoffPolicy, times(1)).onHostAttemptFinished(true)
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(false)
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(true)
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(false)
        verify(performingStrategy, times(1)).performRequest(networkTask)
    }

    @Test
    fun testThreadIsNotRunning() {
        stubbing(rootThreadStateSource) {
            on { this.isRunning } doReturn false
        }
        networkTaskRunnable.run()
        verify(connectionBasedExecutionPolicy, never()).canBeExecuted()
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(any())
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(any())
    }

    @Test
    fun testThreadIsInterrupted() {
        stubbing(connectionBasedExecutionPolicy) {
            on { this.canBeExecuted() } doReturn true
        }
        stubbing(networkTask) {
            on { this.onCreateNetworkTask() } doReturn true
            on { this.shouldTryNextHost() } doReturn true
        }
        stubbing(rootThreadStateSource) {
            on { this.isRunning } doReturnConsecutively listOf(true, true, true, true, true, false)
        }
        networkTaskRunnable.run()
        verify(performingStrategy, times(4)).performRequest(networkTask)
        verify(exponentialBackoffPolicy, times(4)).onHostAttemptFinished(false)
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(true)
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(false)
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(true)
    }

    @Test
    fun testThreadIsNotRunningWhenPerformingRequest() {
        stubbing(rootThreadStateSource) {
            on { this.isRunning } doReturnConsecutively listOf(true, false)
        }
        networkTaskRunnable.run()
        verify(performingStrategy, never()).performRequest(networkTask)
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(any())
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(any())
    }

    @Test
    fun testThreadIsInterruptedWhilePerformingRequest() {
        stubbing(rootThreadStateSource) {
            on { this.isRunning } doReturnConsecutively listOf(true, true, true, true, false)
        }
        stubbing(networkTask) {
            on { this.shouldTryNextHost() } doReturn true
        }
        networkTaskRunnable.run()
        verify(performingStrategy, times(3)).performRequest(networkTask)
        verify(exponentialBackoffPolicy, times(3)).onHostAttemptFinished(false)
        verify(exponentialBackoffPolicy, never()).onHostAttemptFinished(true)
        verify(exponentialBackoffPolicy, times(1)).onAllHostsAttemptsFinished(false)
        verify(exponentialBackoffPolicy, never()).onAllHostsAttemptsFinished(true)
    }

    @Test
    fun testShouldNotExecuteTaskStopsExecuting() {
        stubbing(rootThreadStateSource) {
            on { this.isRunning } doReturn true
        }
        stubbing(connectionBasedExecutionPolicy) {
            on { this.canBeExecuted() } doReturn true
        }
        stubbing(networkTask) {
            on { this.onCreateNetworkTask() } doReturn true
            on { this.shouldTryNextHost() } doReturn false
        }
        networkTaskRunnable.run()
        verify(networkTask, times(1)).onCreateNetworkTask()
    }
}
