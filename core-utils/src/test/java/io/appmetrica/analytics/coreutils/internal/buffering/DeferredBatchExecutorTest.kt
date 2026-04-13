package io.appmetrica.analytics.coreutils.internal.buffering

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.gradle.androidtestutils.rules.LogRule
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DeferredBatchExecutorTest : CommonTest() {

    private val executor: IHandlerExecutor = mock()
    private val buffer: TaskBuffer<String> = AccumulatingTaskBuffer()
    private val processor: BatchProcessor<String> = mock()
    private lateinit var deferredExecutor: DeferredBatchExecutor<String>

    private val delayMillis = 1000L
    private val runnableCaptor = argumentCaptor<Runnable>()

    @get:Rule
    val logRule = LogRule()

    @Before
    fun setUp() {
        deferredExecutor = DeferredBatchExecutor(
            executor = executor,
            buffer = buffer,
            processor = processor,
            delayMillis = delayMillis
        )
    }

    @Test
    fun `submit schedules delayed task when buffer is empty`() {
        deferredExecutor.submit("task1")

        verify(executor).executeDelayed(any(), eq(delayMillis))
    }

    @Test
    fun `submit adds task to buffer`() {
        deferredExecutor.submit("task1")

        assertThat(buffer.isEmpty()).isFalse()
    }

    @Test
    fun `submit does not reschedule when task is already pending`() {
        deferredExecutor.submit("task1")
        deferredExecutor.submit("task2")
        deferredExecutor.submit("task3")

        // Should only schedule once
        verify(executor, times(1)).executeDelayed(any(), eq(delayMillis))
        // Should never remove tasks
        verify(executor, never()).remove(any())
    }

    @Test
    fun `scheduled task processes all accumulated tasks`() {
        deferredExecutor.submit("task1")
        deferredExecutor.submit("task2")
        deferredExecutor.submit("task3")

        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        val tasksCaptor = argumentCaptor<List<String>>()
        verify(processor).processBatch(tasksCaptor.capture())
        assertThat(tasksCaptor.firstValue).containsExactly("task1", "task2", "task3")
    }

    @Test
    fun `scheduled task clears buffer after processing`() {
        deferredExecutor.submit("task1")

        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        assertThat(buffer.isEmpty()).isTrue()
    }

    @Test
    fun `submit with urgent=true processes immediately`() {
        deferredExecutor.submit("urgent_task", urgent = true)

        // Should not schedule delayed task
        verify(executor, never()).executeDelayed(any(), any())

        // Should process immediately
        val tasksCaptor = argumentCaptor<List<String>>()
        verify(processor).processBatch(tasksCaptor.capture())
        assertThat(tasksCaptor.firstValue).containsExactly("urgent_task")
    }

    @Test
    fun `submit with urgent=true cancels pending task`() {
        deferredExecutor.submit("task1")
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))

        deferredExecutor.submit("urgent_task", urgent = true)

        verify(executor).remove(runnableCaptor.firstValue)
    }

    @Test
    fun `submit with urgent=true processes all accumulated tasks`() {
        deferredExecutor.submit("task1")
        deferredExecutor.submit("task2")
        deferredExecutor.submit("urgent_task", urgent = true)

        val tasksCaptor = argumentCaptor<List<String>>()
        verify(processor).processBatch(tasksCaptor.capture())
        assertThat(tasksCaptor.firstValue).containsExactly("task1", "task2", "urgent_task")
    }

    @Test
    fun `flush processes pending tasks immediately`() {
        deferredExecutor.submit("task1")
        deferredExecutor.submit("task2")

        deferredExecutor.flush()

        val tasksCaptor = argumentCaptor<List<String>>()
        verify(processor).processBatch(tasksCaptor.capture())
        assertThat(tasksCaptor.firstValue).containsExactly("task1", "task2")
    }

    @Test
    fun `flush cancels pending scheduled task`() {
        deferredExecutor.submit("task1")
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))

        deferredExecutor.flush()

        verify(executor).remove(runnableCaptor.firstValue)
    }

    @Test
    fun `flush does nothing when buffer is empty`() {
        deferredExecutor.flush()

        verify(processor, never()).processBatch(any())
    }

    @Test
    fun `flush clears buffer after processing`() {
        deferredExecutor.submit("task1")
        deferredExecutor.flush()

        assertThat(buffer.isEmpty()).isTrue()
    }

    @Test
    fun `flushAsync schedules flush on executor`() {
        deferredExecutor.flushAsync()

        verify(executor).execute(any())
    }

    @Test
    fun `flushAsync executes flush when runnable is executed`() {
        deferredExecutor.submit("task1")

        deferredExecutor.flushAsync()
        verify(executor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()

        val tasksCaptor = argumentCaptor<List<String>>()
        verify(processor).processBatch(tasksCaptor.capture())
        assertThat(tasksCaptor.firstValue).containsExactly("task1")
    }

    @Test
    fun `processor exception is caught and logged`() {
        whenever(processor.processBatch(any())).thenThrow(RuntimeException("Test exception"))

        deferredExecutor.submit("task1")
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        // Should not throw exception
        assertThat(buffer.isEmpty()).isTrue()
    }

    @Test
    fun `multiple submit-flush cycles work correctly`() {
        // First cycle
        deferredExecutor.submit("task1")
        deferredExecutor.flush()
        verify(processor).processBatch(listOf("task1"))

        // Second cycle
        deferredExecutor.submit("task2")
        deferredExecutor.flush()
        verify(processor).processBatch(listOf("task2"))

        // Third cycle with multiple tasks
        deferredExecutor.submit("task3")
        deferredExecutor.submit("task4")
        deferredExecutor.flush()
        verify(processor).processBatch(listOf("task3", "task4"))
    }

    @Test
    fun `scheduled task does nothing when buffer is empty`() {
        deferredExecutor.submit("task1")
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))

        // Flush before scheduled task runs
        deferredExecutor.flush()

        // Now run the originally scheduled task
        runnableCaptor.firstValue.run()

        // Should have been called only once (from flush)
        verify(processor, times(1)).processBatch(any())
    }

    @Test
    fun `submit after flush schedules new delayed task`() {
        deferredExecutor.submit("task1")
        deferredExecutor.flush()

        deferredExecutor.submit("task2")

        // Should schedule again
        verify(executor, times(2)).executeDelayed(any(), eq(delayMillis))
    }

    @Test
    fun `buffer is cleared even if processor throws exception`() {
        whenever(processor.processBatch(any())).thenThrow(RuntimeException("Test exception"))

        deferredExecutor.submit("task1")
        deferredExecutor.submit("task2")
        deferredExecutor.flush()

        assertThat(buffer.isEmpty()).isTrue()
    }

    @Test
    fun `custom delay is used when specified`() {
        val customDelay = 5000L
        val customExecutor = DeferredBatchExecutor(
            executor = executor,
            buffer = buffer,
            processor = processor,
            delayMillis = customDelay
        )

        customExecutor.submit("task1")

        verify(executor).executeDelayed(any(), eq(customDelay))
    }
}
