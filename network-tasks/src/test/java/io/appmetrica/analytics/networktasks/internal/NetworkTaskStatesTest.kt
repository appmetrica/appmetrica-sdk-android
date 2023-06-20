package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.coreutils.internal.asserts.DebugAssert
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor
import javax.net.ssl.SSLSocketFactory

@RunWith(RobolectricTestRunner::class)
class NetworkTaskStatesTest {

    private val executor = mock<Executor>()
    private val requestDataHolder = mock<RequestDataHolder>()
    private val responseDataHolder = mock<ResponseDataHolder>()
    private val fullUrlFormer = mock<FullUrlFormer<*>> {
        on { this.hasMoreHosts() } doReturn true
    }
    private val retryPolicyConfig = mock<RetryPolicyConfig>()
    private val sslSocketFactory = mock<SSLSocketFactory>()
    private val description = "some description"
    private val connectionBasedExecutionPolicy = mock<IExecutionPolicy>()
    private val exponentialBackoffPolicy = mock<ExponentialBackoffPolicy>()
    private val underlyingTask = mock<UnderlyingNetworkTask> {
        on { this.fullUrlFormer } doReturn fullUrlFormer
        on { this.requestDataHolder } doReturn requestDataHolder
        on { this.responseDataHolder } doReturn responseDataHolder
        on { this.retryPolicyConfig } doReturn retryPolicyConfig
        on { this.sslSocketFactory } doReturn sslSocketFactory
        on { this.description() } doReturn description
    }
    private val networkTask = NetworkTask(
        executor,
        connectionBasedExecutionPolicy,
        exponentialBackoffPolicy,
        underlyingTask,
        emptyList(),
        "userAgent"
    )

    @get:Rule
    val sDebugAssert = MockedStaticRule(DebugAssert::class.java)

    // region onTaskAdded
    @Test
    fun onTaskAddedEmptyTask() {
        assertThat(networkTask.onTaskAdded()).isTrue
        verify(underlyingTask).onTaskAdded()
    }

    @Test
    fun onTaskAddedPendingTask() {
        networkTask.onTaskAdded()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onTaskAdded()).isFalse
        verify(underlyingTask, never()).onTaskAdded()
    }

    @Test
    fun oTaskAddedCreatedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onTaskAdded()).isFalse
        verify(underlyingTask, never()).onTaskAdded()
    }

    @Test
    fun onTaskAddedExecutingTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onTaskAdded()).isFalse
        verify(underlyingTask, never()).onTaskAdded()
    }

    @Test
    fun onTaskAddedExecutedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestComplete()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onTaskAdded()).isFalse
        verify(underlyingTask, never()).onTaskAdded()
    }

    @Test
    fun onTaskAddedFailedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestError(null)
        clearInvocations(underlyingTask)
        assertThat(networkTask.onTaskAdded()).isFalse
        verify(underlyingTask, never()).onTaskAdded()
    }

    @Test
    fun onTaskAddedUnexecutedTask() {
        networkTask.onTaskAdded()
        networkTask.onShouldNotExecute()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onTaskAdded()).isFalse
        verify(underlyingTask, never()).onTaskAdded()
    }

    @Test
    fun onTaskAddedFinishedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskFinished()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onTaskAdded()).isFalse
        verify(underlyingTask, never()).onTaskAdded()
    }

    @Test
    fun onTaskAddedRemovedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskRemoved()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onTaskAdded()).isFalse
        verify(underlyingTask, never()).onTaskAdded()
    }

    // end region
    // region onCreate
    @Test
    fun onCreateEmptyTask() {
        assertThat(networkTask.onCreateNetworkTask()).isFalse
        verify(underlyingTask, never()).onCreateTask()
    }

    @Test
    fun onCreatePendingTaskTrue() {
        stubbing(underlyingTask) {
            on { this.onCreateTask() } doReturn true
        }
        networkTask.onTaskAdded()
        assertThat(networkTask.onCreateNetworkTask()).isTrue
        verify(underlyingTask).onCreateTask()
    }

    @Test
    fun onCreatePendingTaskFalse() {
        stubbing(underlyingTask) {
            on { this.onCreateTask() } doReturn false
        }
        networkTask.onTaskAdded()
        assertThat(networkTask.onCreateNetworkTask()).isFalse
        verify(underlyingTask).onCreateTask()
    }

    @Test
    fun onCreateAlreadyCreatedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onCreateNetworkTask()).isFalse
        verify(underlyingTask, never()).onCreateTask()
    }

    @Test
    fun onCreateExecutingTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onCreateNetworkTask()).isFalse
        verify(underlyingTask, never()).onCreateTask()
    }

    @Test
    fun onCreateExecutedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestComplete()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onCreateNetworkTask()).isFalse
        verify(underlyingTask, never()).onCreateTask()
    }

    @Test
    fun onCreateFailedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestError(null)
        clearInvocations(underlyingTask)
        assertThat(networkTask.onCreateNetworkTask()).isFalse
        verify(underlyingTask, never()).onCreateTask()
    }

    @Test
    fun onCreateUnexecutedTask() {
        networkTask.onTaskAdded()
        networkTask.onShouldNotExecute()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onCreateNetworkTask()).isFalse
        verify(underlyingTask, never()).onCreateTask()
    }

    @Test
    fun onCreateFinishedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskFinished()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onCreateNetworkTask()).isFalse
        verify(underlyingTask, never()).onCreateTask()
    }

    @Test
    fun onCreateRemovedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskRemoved()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onCreateNetworkTask()).isFalse
        verify(underlyingTask, never()).onCreateTask()
    }

    // end region
    // region onPerformRequest
    @Test
    fun onPerformRequestEmptyTask() {
        assertThat(networkTask.onPerformRequest()).isFalse
        verifyNoInteractions(fullUrlFormer)
        verify(underlyingTask, never()).onPerformRequest()
    }

    @Test
    fun onPerformRequestPendingTask() {
        networkTask.onTaskAdded()
        assertThat(networkTask.onPerformRequest()).isFalse
        verifyNoInteractions(fullUrlFormer)
        verify(underlyingTask, never()).onPerformRequest()
    }

    @Test
    fun onPerformRequestCreatedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        assertThat(networkTask.onPerformRequest()).isTrue
        with(inOrder(fullUrlFormer, underlyingTask)) {
            this.verify(fullUrlFormer).incrementAttemptNumber()
            this.verify(fullUrlFormer).buildAndSetFullHostUrl()
            this.verify(underlyingTask).onPerformRequest()
        }
    }

    @Test
    fun onPerformRequestExecutingTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        clearInvocations(fullUrlFormer, underlyingTask)
        assertThat(networkTask.onPerformRequest()).isFalse
        verifyNoInteractions(fullUrlFormer)
        verify(underlyingTask, never()).onPerformRequest()
    }

    @Test
    fun onPerformRequestSuccessfulTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        stubbing(underlyingTask) {
            on { this.onRequestComplete() } doReturn true
        }
        networkTask.onRequestComplete()
        clearInvocations(fullUrlFormer, underlyingTask)
        assertThat(networkTask.onPerformRequest()).isTrue
        with(inOrder(fullUrlFormer, underlyingTask)) {
            this.verify(fullUrlFormer).incrementAttemptNumber()
            this.verify(fullUrlFormer).buildAndSetFullHostUrl()
            this.verify(underlyingTask).onPerformRequest()
        }
    }

    @Test
    fun onPerformRequestUnsuccessfulTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        stubbing(underlyingTask) {
            on { this.onRequestComplete() } doReturn false
        }
        networkTask.onRequestComplete()
        clearInvocations(fullUrlFormer, underlyingTask)
        assertThat(networkTask.onPerformRequest()).isTrue
        with(inOrder(fullUrlFormer, underlyingTask)) {
            this.verify(fullUrlFormer).incrementAttemptNumber()
            this.verify(fullUrlFormer).buildAndSetFullHostUrl()
            this.verify(underlyingTask).onPerformRequest()
        }
    }

    @Test
    fun onPerformRequestFailedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestError(null)
        clearInvocations(fullUrlFormer, underlyingTask)
        assertThat(networkTask.onPerformRequest()).isTrue
        with(inOrder(fullUrlFormer, underlyingTask)) {
            this.verify(fullUrlFormer).incrementAttemptNumber()
            this.verify(fullUrlFormer).buildAndSetFullHostUrl()
            this.verify(underlyingTask).onPerformRequest()
        }
    }

    @Test
    fun onPerformRequestUnexecutedTask() {
        networkTask.onTaskAdded()
        networkTask.onShouldNotExecute()
        clearInvocations(fullUrlFormer, underlyingTask)
        assertThat(networkTask.onPerformRequest()).isFalse
        verifyNoInteractions(fullUrlFormer)
        verify(underlyingTask, never()).onPerformRequest()
    }

    @Test
    fun onPerformRequestFinishedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskFinished()
        clearInvocations(fullUrlFormer, underlyingTask)
        assertThat(networkTask.onPerformRequest()).isFalse
        verifyNoInteractions(fullUrlFormer)
        verify(underlyingTask, never()).onPerformRequest()
    }

    @Test
    fun onPerformRequestRemovedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskRemoved()
        clearInvocations(fullUrlFormer, underlyingTask)
        assertThat(networkTask.onPerformRequest()).isFalse
        verifyNoInteractions(fullUrlFormer)
        verify(underlyingTask, never()).onPerformRequest()
    }

    // end region
    // region onRequestComplete
    @Test
    fun onRequestCompleteEmptyTask() {
        assertThat(networkTask.onRequestComplete()).isFalse
        verify(underlyingTask, never()).onRequestComplete()
        verify(underlyingTask, never()).onPostRequestComplete(any())
    }

    @Test
    fun onRequestCompletePendingTask() {
        networkTask.onTaskAdded()
        assertThat(networkTask.onRequestComplete()).isFalse
        verify(underlyingTask, never()).onRequestComplete()
        verify(underlyingTask, never()).onPostRequestComplete(any())
    }

    @Test
    fun onRequestCompleteCreatedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        assertThat(networkTask.onRequestComplete()).isFalse
        verify(underlyingTask, never()).onRequestComplete()
        verify(underlyingTask, never()).onPostRequestComplete(any())
    }

    @Test
    fun onRequestCompleteExecutingTaskSuccess() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        stubbing(underlyingTask) {
            on { this.onRequestComplete() } doReturn true
        }
        assertThat(networkTask.onRequestComplete()).isTrue
        verify(underlyingTask).onRequestComplete()
        verify(underlyingTask).onPostRequestComplete(true)
    }

    @Test
    fun onRequestCompleteExecutingTaskFailure() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        stubbing(underlyingTask) {
            on { this.onRequestComplete() } doReturn false
        }
        assertThat(networkTask.onRequestComplete()).isFalse
        verify(underlyingTask).onRequestComplete()
        verify(underlyingTask).onPostRequestComplete(false)
    }

    @Test
    fun onRequestCompleteAlreadyExecutedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestComplete()
        clearInvocations(underlyingTask)
        assertThat(networkTask.onRequestComplete()).isFalse
        verify(underlyingTask, never()).onRequestComplete()
        verify(underlyingTask, never()).onPostRequestComplete(any())
    }

    @Test
    fun onRequestCompleteFailedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestError(null)
        assertThat(networkTask.onRequestComplete()).isFalse
        verify(underlyingTask, never()).onRequestComplete()
        verify(underlyingTask, never()).onPostRequestComplete(any())
    }

    @Test
    fun onRequestCompleteUnexecutedTask() {
        networkTask.onTaskAdded()
        networkTask.onShouldNotExecute()
        assertThat(networkTask.onRequestComplete()).isFalse
        verify(underlyingTask, never()).onRequestComplete()
        verify(underlyingTask, never()).onPostRequestComplete(any())
    }

    @Test
    fun onRequestCompleteFinishedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskFinished()
        assertThat(networkTask.onRequestComplete()).isFalse
        verify(underlyingTask, never()).onRequestComplete()
        verify(underlyingTask, never()).onPostRequestComplete(any())
    }

    @Test
    fun onRequestCompleteRemovedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskRemoved()
        assertThat(networkTask.onRequestComplete()).isFalse
        verify(underlyingTask, never()).onRequestComplete()
        verify(underlyingTask, never()).onPostRequestComplete(any())
    }

    // end region
    // region onRequestError
    @Test
    fun onRequestErrorEmptyTask() {
        networkTask.onRequestError(null)
        verify(underlyingTask, never()).onRequestError(anyOrNull())
    }

    @Test
    fun onRequestErrorPendingTask() {
        networkTask.onTaskAdded()
        networkTask.onRequestError(null)
        verify(underlyingTask, never()).onRequestError(anyOrNull())
    }

    @Test
    fun onRequestErrorPreparingTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onRequestError(null)
        verify(underlyingTask, never()).onRequestError(anyOrNull())
    }

    @Test
    fun onRequestErrorExecutingTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestError(null)
        verify(underlyingTask,).onRequestError(anyOrNull())
    }

    @Test
    fun onRequestErrorUnexecutedTask() {
        networkTask.onTaskAdded()
        networkTask.onShouldNotExecute()
        networkTask.onRequestError(null)
        verify(underlyingTask, never()).onRequestError(anyOrNull())
    }

    @Test
    fun onRequestErrorFinishedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestError(null)
        networkTask.onTaskFinished()
        clearInvocations(underlyingTask)
        networkTask.onRequestError(null)
        verify(underlyingTask, never()).onRequestError(anyOrNull())
    }

    @Test
    fun onRequestErrorRemovedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestError(null)
        networkTask.onTaskRemoved()
        clearInvocations(underlyingTask)
        networkTask.onRequestError(null)
        verify(underlyingTask, never()).onRequestError(anyOrNull())
    }

    // end region
    // region onShouldNotExecute
    @Test
    fun onShouldNotExecuteEmptyTask() {
        networkTask.onShouldNotExecute()
        networkTask.onTaskAdded()
        verify(underlyingTask).onTaskAdded()
    }

    // end region
    // region onTaskFinished
    @Test
    fun onTaskFinishedEmptyTask() {
        networkTask.onTaskFinished()
        verify(underlyingTask, never()).onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
    }

    @Test
    fun onTaskFinishedPendingTask() {
        networkTask.onTaskAdded()
        networkTask.onTaskFinished()
        verify(underlyingTask).onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
        networkTask.onCreateNetworkTask()
        verify(underlyingTask, never()).onCreateTask()
    }

    @Test
    fun onTaskFinishedCreatedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskFinished()
        verify(underlyingTask).onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
        networkTask.onPerformRequest()
        verifyNoInteractions(fullUrlFormer)
    }

    @Test
    fun onTaskFinishedExecutingTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onTaskFinished()
        verify(underlyingTask).onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
        networkTask.onRequestComplete()
        verify(underlyingTask, never()).onRequestComplete()
    }

    @Test
    fun onTaskFinishedExecutedTaskSuccess() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        stubbing(underlyingTask) {
            on { this.onRequestComplete() } doReturn true
        }
        networkTask.onRequestComplete()
        networkTask.onTaskFinished()
        verify(underlyingTask).onTaskFinished()
        verify(underlyingTask).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
    }

    @Test
    fun onTaskFinishedExecutedTaskFailure() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        stubbing(underlyingTask) {
            on { this.onRequestComplete() } doReturn false
        }
        networkTask.onRequestComplete()
        networkTask.onTaskFinished()
        verify(underlyingTask).onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask).onUnsuccessfulTaskFinished()
    }

    @Test
    fun onTaskFinishedFailedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestError(null)
        networkTask.onTaskFinished()
        verify(underlyingTask).onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask).onUnsuccessfulTaskFinished()
    }

    @Test
    fun onTaskFinishedUnexecutedTask() {
        networkTask.onTaskAdded()
        networkTask.onShouldNotExecute()
        networkTask.onTaskFinished()
        verify(underlyingTask).onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask).onUnsuccessfulTaskFinished()
    }

    @Test
    fun onTaskFinishedAlreadyFinishedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskFinished()
        clearInvocations(underlyingTask)
        networkTask.onTaskFinished()
        verify(underlyingTask, never()).onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
    }

    @Test
    fun onTaskFinishedRemovedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskRemoved()
        clearInvocations(underlyingTask)
        networkTask.onTaskFinished()
        verify(underlyingTask, never()).onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
    }

    // end region
    // region onTaskRemoved
    @Test
    fun onTaskRemovedEmptyTask() {
        networkTask.onTaskRemoved()
        assertThat(networkTask.shouldTryNextHost()).isTrue
    }

    @Test
    fun onTaskRemovedPendingTask() {
        networkTask.onTaskAdded()
        networkTask.onTaskRemoved()
        assertThat(networkTask.shouldTryNextHost()).isFalse
        networkTask.onCreateNetworkTask()
        verify(underlyingTask, never()).onCreateTask()
    }

    @Test
    fun onTaskRemovedCreatedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskRemoved()
        assertThat(networkTask.shouldTryNextHost()).isFalse
        clearInvocations(fullUrlFormer)
        networkTask.onPerformRequest()
        verifyNoInteractions(fullUrlFormer)
    }

    @Test
    fun onTaskRemovedExecutingTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onTaskRemoved()
        assertThat(networkTask.shouldTryNextHost()).isFalse
        networkTask.onRequestComplete()
        verify(underlyingTask, never()).onRequestComplete()
    }

    @Test
    fun onTaskRemovedExecutedTaskSuccess() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        stubbing(underlyingTask) {
            on { this.onRequestComplete() } doReturn true
        }
        networkTask.onRequestComplete()
        networkTask.onTaskRemoved()
        assertThat(networkTask.shouldTryNextHost()).isFalse
        networkTask.onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
        verify(underlyingTask, never()).onTaskFinished()
    }

    @Test
    fun onTaskRemovedExecutedTaskFailure() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        stubbing(underlyingTask) {
            on { this.onRequestComplete() } doReturn false
        }
        networkTask.onRequestComplete()
        networkTask.onTaskRemoved()
        assertThat(networkTask.shouldTryNextHost()).isFalse
        networkTask.onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
        verify(underlyingTask, never()).onTaskFinished()
    }

    @Test
    fun onTaskRemovedFailedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onPerformRequest()
        networkTask.onRequestError(null)
        networkTask.onTaskRemoved()
        assertThat(networkTask.shouldTryNextHost()).isFalse
        networkTask.onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
        verify(underlyingTask, never()).onTaskFinished()
    }

    @Test
    fun onTaskRemovedUnexecutedTask() {
        networkTask.onTaskAdded()
        networkTask.onShouldNotExecute()
        networkTask.onTaskRemoved()
        assertThat(networkTask.shouldTryNextHost()).isFalse
        networkTask.onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
        verify(underlyingTask, never()).onTaskFinished()
    }

    @Test
    fun onTaskRemovedFinishedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskFinished()
        clearInvocations(underlyingTask)
        networkTask.onTaskRemoved()
        assertThat(networkTask.shouldTryNextHost()).isFalse
        networkTask.onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
        verify(underlyingTask, never()).onTaskFinished()
    }

    @Test
    fun onTaskRemovedAlreadyRemovedTask() {
        networkTask.onTaskAdded()
        networkTask.onCreateNetworkTask()
        networkTask.onTaskRemoved()
        clearInvocations(underlyingTask)
        networkTask.onTaskRemoved()
        assertThat(networkTask.shouldTryNextHost()).isFalse
        networkTask.onTaskFinished()
        verify(underlyingTask, never()).onSuccessfulTaskFinished()
        verify(underlyingTask, never()).onUnsuccessfulTaskFinished()
        verify(underlyingTask, never()).onTaskFinished()
    }
    // end region
}
