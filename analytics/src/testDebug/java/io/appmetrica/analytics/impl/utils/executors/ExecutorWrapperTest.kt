package io.appmetrica.analytics.impl.utils.executors

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutorExtensions.postDelayed
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.Collections
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration

@SuppressLint("RobolectricUsage") // Real HandlerThread verifies queue ordering and cancellation.
@RunWith(RobolectricTestRunner::class)
internal class ExecutorWrapperTest : CommonTest() {

    private val handlerThread: InterruptionSafeHandlerThread = mock()
    private val looper: Looper = mock()
    private val handler: Handler = mock()
    private val runnable: Runnable = mock()
    private val callableResult = Any()

    private lateinit var executorWrapper: ExecutorWrapper

    @Before
    fun setUp() {
        whenever(handlerThread.looper).thenReturn(looper)
        executorWrapper = ExecutorWrapper(handlerThread, looper, handler)
    }

    @Test
    fun `constructors expose handler and looper`() {
        val executorWithThread = ExecutorWrapper(handlerThread)

        assertThat(executorWithThread.getLooper()).isEqualTo(looper)
        assertThat(executorWithThread.getHandler().getLooper()).isEqualTo(looper)
        assertThat(executorWrapper.getHandler()).isEqualTo(handler)
        assertThat(executorWrapper.getLooper()).isEqualTo(looper)
    }

    @Test
    fun `thread name constructor starts its handler thread`() {
        val actualExecutor = ExecutorWrapper(THREAD_NAME)

        try {
            assertThat(actualExecutor.getLooper().getThread().getName()).startsWith(THREAD_NAME)
            assertThat(actualExecutor.getLooper().getThread().getState()).isNotEqualTo(Thread.State.NEW)
        } finally {
            actualExecutor.getLooper().quitSafely()
            actualExecutor.getLooper().getThread().join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS))
        }
    }

    @Test
    fun `post execute and submit enqueue tasks`() {
        executorWrapper.post(runnable)
        executorWrapper.execute(runnable)
        val future = executorWrapper.submit(Callable { callableResult })

        val runnableCaptor = argumentCaptor<Runnable>()
        verify(handler, times(3)).post(runnableCaptor.capture())
        assertThat(runnableCaptor.allValues.take(2)).containsExactly(runnable, runnable)

        runnableCaptor.thirdValue.run()

        assertThat(future.get()).isEqualTo(callableResult)
    }

    @Test
    fun `postDelayed preserves delay conversion`() {
        executorWrapper.postDelayed(runnable, DELAY)
        executorWrapper.postDelayed(runnable, DELAY, TimeUnit.SECONDS)

        val delayCaptor = argumentCaptor<Long>()
        verify(handler, times(2)).postDelayed(eq(runnable), delayCaptor.capture())
        assertThat(delayCaptor.allValues).containsExactly(
            DELAY,
            TimeUnit.SECONDS.toMillis(DELAY),
        )
    }

    @Test
    fun `execute keeps nested tasks queued`() = withExecutor { actualExecutor ->
        val order = Collections.synchronizedList(mutableListOf<String>())
        val nestedTaskCompleted = CountDownLatch(1)

        actualExecutor.post {
            order += "outer-start"
            actualExecutor.execute {
                order += "nested"
                nestedTaskCompleted.countDown()
            }
            order += "outer-end"
        }

        assertThat(nestedTaskCompleted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()
        assertThat(order).containsExactly("outer-start", "outer-end", "nested")
    }

    @Test
    fun `zero delay and submit stay queued`() = withExecutor { actualExecutor ->
        val order = Collections.synchronizedList(mutableListOf<String>())
        val tasksCompleted = CountDownLatch(2)

        actualExecutor.post {
            actualExecutor.postDelayed(
                {
                    order += "zero-delay"
                    tasksCompleted.countDown()
                },
                Duration.ZERO
            )
            val nestedFuture = actualExecutor.submit(
                Callable {
                    order += "nested-future"
                    tasksCompleted.countDown()
                    callableResult
                }
            )
            assertThat(nestedFuture.isDone()).isFalse()
            order += "outer-end"
        }

        assertThat(tasksCompleted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()
        assertThat(order).containsExactly("outer-end", "zero-delay", "nested-future")
    }

    @Test
    fun `submit reports result and failure`() = withExecutor { actualExecutor ->
        val future = actualExecutor.submit(Callable { callableResult })
        val failure = IllegalStateException("test failure")
        val failedFuture = actualExecutor.submit(Callable<Any> { throw failure })

        assertThat(future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo(callableResult)
        assertThatThrownBy { failedFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }
            .isInstanceOf(ExecutionException::class.java)
            .hasCause(failure)
    }

    @Test
    fun `cancel and remove suppress queued tasks`() = withExecutor { actualExecutor ->
        val blockingTaskStarted = CountDownLatch(1)
        val releaseBlockingTask = CountDownLatch(1)
        val removedTaskExecuted = AtomicBoolean(false)
        val removableTask = Runnable { removedTaskExecuted.set(true) }

        try {
            actualExecutor.post {
                blockingTaskStarted.countDown()
                releaseBlockingTask.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            }
            assertThat(blockingTaskStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()

            val cancelledFuture = actualExecutor.submit(Callable { error("Cancelled task ran") })
            actualExecutor.postDelayed(removableTask, Duration.ZERO)
            actualExecutor.remove(removableTask)
            val queueDrained = CountDownLatch(1)
            actualExecutor.post { queueDrained.countDown() }

            assertThat(cancelledFuture.isDone()).isFalse()
            assertThat(cancelledFuture.cancel(false)).isTrue()
            releaseBlockingTask.countDown()

            assertThat(queueDrained.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()
            assertThat(cancelledFuture.isCancelled()).isTrue()
            assertThat(removedTaskExecuted.get()).isFalse()
        } finally {
            releaseBlockingTask.countDown()
        }
    }

    private fun withExecutor(block: (ExecutorWrapper) -> Unit) {
        val handlerThread = InterruptionSafeHandlerThread(THREAD_NAME).apply { start() }
        try {
            block(ExecutorWrapper(handlerThread))
        } finally {
            handlerThread.quitSafely()
            handlerThread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS))
        }
    }

    @Test
    fun `remove lifecycle delegates to handler thread`() {
        executorWrapper.remove(runnable)
        executorWrapper.removeAll()
        whenever(handlerThread.isRunning()).thenReturn(true, false)

        assertThat(executorWrapper.isRunning()).isTrue()
        assertThat(executorWrapper.isRunning()).isFalse()

        executorWrapper.stopRunning()

        verify(handler).removeCallbacks(runnable)
        verify(handler).removeCallbacksAndMessages(null)
        verify(handlerThread).stopRunning()
    }

    companion object {
        private const val THREAD_NAME = "TestThread"
        private const val DELAY = 123123L
        private const val TIMEOUT_SECONDS = 5L
    }
}
