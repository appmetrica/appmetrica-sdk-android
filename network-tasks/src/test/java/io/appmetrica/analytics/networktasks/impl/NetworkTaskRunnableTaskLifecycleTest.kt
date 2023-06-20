package io.appmetrica.analytics.networktasks.impl

import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.networktasks.internal.ExponentialBackoffPolicy
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkTaskRunnableTaskLifecycleTest {

    private val retryPolicyConfig = mock<RetryPolicyConfig>()
    private val performingStrategy = mock<NetworkTaskPerformingStrategy>()
    private val rootThreadStateSource = mock<InterruptionSafeThread> {
        on { this.isRunning } doReturn true
    }
    private val exponentialBackoffPolicy = mock<ExponentialBackoffPolicy> {
        on { this.canBeExecuted(retryPolicyConfig) } doReturn true
    }
    private val connectionBasedExecutionPolicy = mock<IExecutionPolicy> {
        on { this.canBeExecuted() } doReturn true
    }
    private val networkTask = mock<NetworkTask> {
        on { this.exponentialBackoffPolicy } doReturn exponentialBackoffPolicy
        on { this.connectionExecutionPolicy } doReturn connectionBasedExecutionPolicy
        on { this.shouldTryNextHost() } doReturn false
        on { this.onCreateNetworkTask() } doReturn true
        on { this.retryPolicyConfig } doReturn retryPolicyConfig
    }
    private val networkTaskRunnable = NetworkTaskRunnable(
        networkTask,
        rootThreadStateSource,
        performingStrategy
    )

    @Test
    fun notCreatedNetworkTaskLifecycleTest() {
        stubbing(networkTask) {
            on { this.onCreateNetworkTask() } doReturn false
        }
        networkTaskRunnable.run()
        verify(networkTask).onCreateNetworkTask()
        verifyZeroInteractions(performingStrategy)
    }

    @Test
    fun offlineNetworkTaskLifecycleTest() {
        stubbing(connectionBasedExecutionPolicy) {
            on { this.canBeExecuted() } doReturn false
        }
        networkTaskRunnable.run()
        val inOrder = inOrder(networkTask, performingStrategy)
        inOrder.verify(networkTask).onShouldNotExecute()
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun retryingTooSoonNetworkTaskLifecycleTest() {
        stubbing(exponentialBackoffPolicy) {
            on { this.canBeExecuted(retryPolicyConfig) } doReturn false
        }
        networkTaskRunnable.run()
        val inOrder = Mockito.inOrder(networkTask, performingStrategy)
        inOrder.verify(networkTask).onShouldNotExecute()
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun successfulNetworkTaskLifecycleTest() {
        networkTaskRunnable.run()
        val inOrder = inOrder(networkTask, performingStrategy)
        inOrder.verify(networkTask).onCreateNetworkTask()
        inOrder.verify(performingStrategy).performRequest(networkTask)
        inOrder.verify(networkTask).shouldTryNextHost()
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun retriedNetworkTaskLifecycleTest() {
        Mockito.`when`(networkTask.shouldTryNextHost()).thenReturn(true, false)
        networkTaskRunnable.run()
        val inOrder = Mockito.inOrder(networkTask, performingStrategy)
        inOrder.verify(networkTask).onCreateNetworkTask()
        inOrder.verify(performingStrategy).performRequest(networkTask)
        inOrder.verify(networkTask).shouldTryNextHost()
        inOrder.verify(performingStrategy).performRequest(networkTask)
        inOrder.verify(networkTask).shouldTryNextHost()
        inOrder.verifyNoMoreInteractions()
    }
}
