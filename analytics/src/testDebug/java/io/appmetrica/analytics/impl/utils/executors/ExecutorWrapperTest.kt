package io.appmetrica.analytics.impl.utils.executors

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutorExtensions.postDelayed
import io.appmetrica.analytics.coreapi.internal.executors.QueuedExecutor
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
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
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.Collections
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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
    fun `post and submit enqueue tasks`() {
        executorWrapper.post(runnable)
        val future = executorWrapper.submit(Callable { callableResult })

        val runnableCaptor = argumentCaptor<Runnable>()
        verify(handler, times(2)).post(runnableCaptor.capture())
        assertThat(runnableCaptor.firstValue).isSameAs(runnable)

        runnableCaptor.secondValue.run()

        assertThat(future.get()).isEqualTo(callableResult)
    }

    @Test
    fun `execute posts original task from foreign or absent looper`() {
        withExecutor { foreignExecutor ->
            foreignExecutor.submit(
                Callable {
                    assertThat(Looper.myLooper()).isSameAs(foreignExecutor.getLooper()).isNotSameAs(looper)
                    executorWrapper.execute(runnable)
                }
            ).get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }

        val executeWithoutLooper = FutureTask(
            Callable {
                assertThat(Looper.myLooper()).isNull()
                executorWrapper.execute(runnable)
            }
        )
        val thread = Thread(executeWithoutLooper)
        thread.start()
        try {
            executeWithoutLooper.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        } finally {
            thread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS))
        }

        verifyNoInteractions(runnable)
        val runnableCaptor = argumentCaptor<Runnable>()
        verify(handler, times(2)).post(runnableCaptor.capture())
        runnableCaptor.allValues.forEach { task ->
            assertThat(task).isSameAs(runnable)
            task.run()
        }
        verify(runnable, times(2)).run()
    }

    @Test
    fun `postDelayed preserves delay conversion`() {
        executorWrapper.postDelayed(runnable, DELAY.milliseconds)
        executorWrapper.postDelayed(runnable, DELAY, TimeUnit.SECONDS)

        val delayCaptor = argumentCaptor<Long>()
        verify(handler, times(2)).postDelayed(eq(runnable), delayCaptor.capture())
        assertThat(delayCaptor.allValues).containsExactly(
            DELAY,
            TimeUnit.SECONDS.toMillis(DELAY),
        )
    }

    @Test
    fun `execute runs nested tasks inline before pending posts`() = withExecutor { actualExecutor ->
        val order = Collections.synchronizedList(mutableListOf<String>())
        val tasksCompleted = CountDownLatch(2)
        val inlineCalls = AtomicInteger()

        actualExecutor.post {
            order += "outer-start"
            actualExecutor.post { order += "pending" }
            actualExecutor.execute {
                order += "nested-start"
                actualExecutor.execute {
                    inlineCalls.incrementAndGet()
                    order += "inner"
                }
                order += "nested-end"
            }
            actualExecutor.post {
                order += "posted"
                tasksCompleted.countDown()
            }
            QueuedExecutor(actualExecutor).execute {
                order += "adapter"
                tasksCompleted.countDown()
            }
            order += "outer-end"
        }

        assertThat(tasksCompleted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()
        assertThat(inlineCalls.get()).isEqualTo(1)
        assertThat(order).containsExactly(
            "outer-start", "nested-start", "inner", "nested-end", "outer-end", "pending", "posted", "adapter"
        )
    }

    @Test
    fun `execute uses looper identity for threads with equal names`() = withExecutor { firstExecutor ->
        withExecutor { secondExecutor ->
            val firstLooper = firstExecutor.getLooper()
            val secondLooper = secondExecutor.getLooper()
            val taskCompleted = CountDownLatch(1)
            val foreignCalls = AtomicInteger()
            val ranOnSecondLooper = AtomicBoolean()

            assertThat(firstLooper.getThread().getName()).isEqualTo(secondLooper.getThread().getName())
            assertThat(firstLooper).isNotSameAs(secondLooper)

            val outer = firstExecutor.submit(
                Callable {
                    var inlineCalls = 0
                    firstExecutor.execute {
                        assertThat(Looper.myLooper()).isSameAs(firstLooper)
                        assertThat(Thread.currentThread()).isSameAs(firstLooper.getThread())
                        inlineCalls++
                    }
                    assertThat(inlineCalls).isEqualTo(1)
                    secondExecutor.execute {
                        ranOnSecondLooper.set(Looper.myLooper() === secondLooper)
                        foreignCalls.incrementAndGet()
                        taskCompleted.countDown()
                    }
                }
            )

            outer.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            assertThat(taskCompleted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()
            assertThat(ranOnSecondLooper.get()).isTrue()
            assertThat(foreignCalls.get()).isEqualTo(1)
        }
    }

    @Test
    fun `inline failure propagates and queued safe failure allows following tasks`() = withExecutor { actualExecutor ->
        val failure = IllegalStateException("test failure")
        val safeCalls = AtomicInteger()
        val queueDrained = CountDownLatch(1)
        val outer = actualExecutor.submit(
            Callable {
                assertThatThrownBy { actualExecutor.execute { throw failure } }.isSameAs(failure)
                actualExecutor.post(
                    object : SafeRunnable() {
                        override fun runSafety() {
                            safeCalls.incrementAndGet()
                            throw failure
                        }
                    }
                )
                actualExecutor.post { queueDrained.countDown() }
            }
        )

        outer.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        assertThat(queueDrained.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()
        assertThat(safeCalls.get()).isEqualTo(1)
    }

    @Test
    fun `removal suppresses pending tasks without cancelling futures`() = withExecutor { actualExecutor ->
        val removedCalls = AtomicInteger()
        val singleRemovalDrained = CountDownLatch(1)
        val queueDrained = CountDownLatch(1)
        actualExecutor.post {
            val removableTask = Runnable { removedCalls.incrementAndGet() }
            actualExecutor.post(removableTask)
            actualExecutor.remove(removableTask)
            actualExecutor.post { singleRemovalDrained.countDown() }
        }
        assertThat(singleRemovalDrained.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()
        assertThat(removedCalls.get()).isZero()

        val outer = actualExecutor.submit(
            Callable {
                actualExecutor.post { removedCalls.incrementAndGet() }
                val removedFuture = actualExecutor.submit(Callable { removedCalls.incrementAndGet() })
                actualExecutor.removeAll()
                actualExecutor.post { queueDrained.countDown() }
                removedFuture
            }
        )

        val removedFuture = outer.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        assertThat(queueDrained.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()
        assertThat(removedCalls.get()).isZero()
        assertThat(removedFuture.isDone()).isFalse()
        assertThat(removedFuture.isCancelled()).isFalse()
        assertThat(removedFuture.cancel(false)).isTrue()
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
