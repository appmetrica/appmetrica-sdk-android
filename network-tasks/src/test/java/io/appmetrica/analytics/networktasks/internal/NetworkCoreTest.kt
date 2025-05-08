package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.networktasks.impl.NetworkTaskRunnable
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stubbing
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.lang.Thread.sleep
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
internal class NetworkCoreTest : CommonTest() {

    private val description = "my task"
    private val executor = mock<Executor>()
    private val networkTaskRunnable = mock<NetworkTaskRunnable>()
    private val runnableProvider = mock<NetworkTaskRunnable.Provider> {
        on { this.create(any(), any()) } doReturn networkTaskRunnable
    }
    private val networkTask = mock<NetworkTask> {
        on { this.executor } doReturn executor
        on { this.onTaskAdded() } doReturn true
        on { this.description() } doReturn description
    }
    private val networkCore =
        NetworkCore(runnableProvider)

    @Before
    fun setUp() {
        networkCore.start()
    }

    @After
    fun tearDown() {
        networkCore.stopTasks()
        try {
            networkCore.stopRunning()
        } catch (ignored: Throwable) { }
    }

    @Test
    fun addTaskOnTaskAddedReturnsFalse() {
        stubbing(networkTask) {
            on { this.onTaskAdded() } doReturn false
        }
        networkCore.startTask(networkTask)
        verify(executor, timeout(500).times(0)).execute(any())
    }

    @Test
    fun addTaskToStoppedThread() {
        networkCore.stopRunning()
        networkCore.startTask(networkTask)
        verify(executor, timeout(500).times(0)).execute(any())
    }

    @Test
    fun addAlreadyAddedTask() {
        whenever(executor.execute(any())).doAnswer { sleep(1500) }
        networkCore.startTask(networkTask)
        verify(executor, timeout(500).times(1)).execute(networkTaskRunnable)
        clearInvocations(executor)
        val anotherTask = mock<NetworkTask> {
            on { this.executor } doReturn executor
            on { this.onTaskAdded() } doReturn true
            on { this.description() } doReturn description
        }
        networkCore.startTask(anotherTask)
        verify(executor, timeout(1500).times(0)).execute(networkTaskRunnable)

        sleep(2000)
        networkCore.startTask(anotherTask)
        verify(executor, timeout(1500).times(1)).execute(networkTaskRunnable)
    }

    @Test
    fun addAndExecuteTask() {
        networkCore.startTask(networkTask)
        val inOrder = inOrder(executor, networkTask)
        inOrder.verify(executor, timeout(500).times(1)).execute(networkTaskRunnable)
        inOrder.verify(networkTask).onTaskFinished()
        inOrder.verify(networkTask).onTaskRemoved()
    }

    @Test
    fun addAndExecuteMultipleDifferentTasks() {
        doAnswer { sleep(1000) }.whenever(executor).execute(any())
        networkCore.startTask(networkTask)
        verify(executor, timeout(500).times(1)).execute(networkTaskRunnable)
        clearInvocations(executor)
        val anotherTask = mock<NetworkTask> {
            on { this.executor } doReturn executor
            on { this.onTaskAdded() } doReturn true
            on { this.description() } doReturn "another description"
        }
        networkCore.startTask(anotherTask)
        verify(executor, timeout(1500).times(1)).execute(networkTaskRunnable)
    }

    @Test
    fun destroyWhileHasRunningTask() {
        doAnswer { sleep(1000) }.whenever(executor).execute(any())
        networkCore.startTask(networkTask)
        verify(executor, timeout(500).times(1)).execute(networkTaskRunnable)
        networkCore.stopTasks()
        verify(networkTask, never()).onTaskRemoved()
    }

    @Test
    fun destroyWithNoRunningTask() {
        networkCore.stopTasks()
    }

    @Test
    fun destroyRemovesAllTasksFromQueue() {
        val task1 = mock<NetworkTask> {
            on { this.onTaskAdded() } doReturn true
            on { this.executor } doReturn executor
            on { this.description() } doReturn "task1"
        }
        val task2 = mock<NetworkTask> {
            on { this.onTaskAdded() } doReturn true
            on { this.executor } doReturn executor
            on { this.description() } doReturn "task2"
        }
        doAnswer { sleep(1000) }.whenever(executor).execute(any())
        networkCore.startTask(networkTask)
        networkCore.startTask(task1)
        networkCore.startTask(task2)
        networkCore.stopTasks()
        verify(task1).onTaskRemoved()
        verify(task2).onTaskRemoved()
    }
}
