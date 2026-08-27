package io.appmetrica.analytics.impl.db

import android.content.ContentValues
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.gradle.androidtestutils.rules.LogRule
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class BufferedEventsWriterTest : CommonTest() {

    private lateinit var executor: IHandlerExecutor
    private lateinit var writer: EventBatchWriter
    private lateinit var bufferedWriter: BufferedEventsWriter

    private val delayMillis = 1000L
    private val runnableCaptor = argumentCaptor<Runnable>()
    private val eventsCaptor = argumentCaptor<List<ContentValues>>()

    @get:Rule
    val logRule = LogRule()

    @Before
    fun setUp() {
        executor = mock()
        writer = mock()
        bufferedWriter = BufferedEventsWriter(
            writer = writer,
            executor = executor,
            delayMillis = delayMillis
        )
    }

    @Test
    fun `addEvent schedules delayed write`() {
        val event = ContentValues()

        bufferedWriter.addEvent(event, isUrgent = false)

        verify(executor).executeDelayed(any(), eq(delayMillis))
    }

    @Test
    fun `addEvent does not reschedule when task is already pending`() {
        val event1 = ContentValues()
        val event2 = ContentValues()
        val event3 = ContentValues()

        bufferedWriter.addEvent(event1, isUrgent = false)
        bufferedWriter.addEvent(event2, isUrgent = false)
        bufferedWriter.addEvent(event3, isUrgent = false)

        // Should only schedule once
        verify(executor, times(1)).executeDelayed(any(), eq(delayMillis))
        // Should never remove tasks
        verify(executor, never()).remove(any())
    }

    @Test
    fun `scheduled task writes all accumulated events`() {
        val event1 = ContentValues().apply { put("key1", "value1") }
        val event2 = ContentValues().apply { put("key2", "value2") }
        val event3 = ContentValues().apply { put("key3", "value3") }

        bufferedWriter.addEvent(event1, isUrgent = false)
        bufferedWriter.addEvent(event2, isUrgent = false)
        bufferedWriter.addEvent(event3, isUrgent = false)

        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        verify(writer).writeEvents(eventsCaptor.capture())
        assertThat(eventsCaptor.firstValue).hasSize(3)
    }

    @Test
    fun `scheduled task notifies listeners after writing`() {
        val event = ContentValues()

        bufferedWriter.addEvent(event, isUrgent = false)

        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        verify(writer).writeEvents(any())
        verify(writer).notifyListeners(any())
    }

    @Test
    fun `addEvent with urgent=true writes immediately`() {
        val event = ContentValues()

        bufferedWriter.addEvent(event, isUrgent = true)

        // Should not schedule delayed task
        verify(executor, never()).executeDelayed(any(), any())

        // Should write immediately
        verify(writer).writeEvents(any())
        verify(writer).notifyListeners(any())
    }

    @Test
    fun `addEvent with urgent=true cancels pending task`() {
        val event1 = ContentValues()
        val urgentEvent = ContentValues()

        bufferedWriter.addEvent(event1, isUrgent = false)
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))

        bufferedWriter.addEvent(urgentEvent, isUrgent = true)

        verify(executor).remove(runnableCaptor.firstValue)
    }

    @Test
    fun `addEvent with urgent=true writes all accumulated events`() {
        val event1 = ContentValues().apply { put("key1", "value1") }
        val event2 = ContentValues().apply { put("key2", "value2") }
        val urgentEvent = ContentValues().apply { put("key3", "urgent") }

        bufferedWriter.addEvent(event1, isUrgent = false)
        bufferedWriter.addEvent(event2, isUrgent = false)
        bufferedWriter.addEvent(urgentEvent, isUrgent = true)

        verify(writer).writeEvents(eventsCaptor.capture())
        assertThat(eventsCaptor.firstValue).hasSize(3)
    }

    @Test
    fun `flush writes pending events immediately`() {
        val event1 = ContentValues()
        val event2 = ContentValues()

        bufferedWriter.addEvent(event1, isUrgent = false)
        bufferedWriter.addEvent(event2, isUrgent = false)

        bufferedWriter.flush()

        verify(writer).writeEvents(eventsCaptor.capture())
        assertThat(eventsCaptor.firstValue).hasSize(2)
    }

    @Test
    fun `flush cancels pending scheduled task`() {
        val event = ContentValues()

        bufferedWriter.addEvent(event, isUrgent = false)
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))

        bufferedWriter.flush()

        verify(executor).remove(runnableCaptor.firstValue)
    }

    @Test
    fun `flush does nothing when no pending events`() {
        bufferedWriter.flush()

        verify(writer, never()).writeEvents(any())
        verify(writer, never()).notifyListeners(any())
    }

    @Test
    fun `flush notifies listeners after writing`() {
        val event = ContentValues()

        bufferedWriter.addEvent(event, isUrgent = false)
        bufferedWriter.flush()

        verify(writer).writeEvents(any())
        verify(writer).notifyListeners(any())
    }

    @Test
    fun `flushAsync schedules flush on executor`() {
        bufferedWriter.flushAsync()

        verify(executor).execute(any())
    }

    @Test
    fun `flushAsync executes flush when runnable runs`() {
        val event = ContentValues()

        bufferedWriter.addEvent(event, isUrgent = false)
        bufferedWriter.flushAsync()

        verify(executor).execute(runnableCaptor.capture())
        runnableCaptor.firstValue.run()

        verify(writer).writeEvents(any())
        verify(writer).notifyListeners(any())
    }

    @Test
    fun `writer exception is caught and logged`() {
        whenever(writer.writeEvents(any())).doThrow(RuntimeException("Test exception"))

        val event = ContentValues()
        bufferedWriter.addEvent(event, isUrgent = false)

        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        // Should not throw exception
        verify(writer).writeEvents(any())
        // Should not notify listeners if write failed
        verify(writer, never()).notifyListeners(any())
    }

    @Test
    fun `notifyListeners exception is caught and logged`() {
        whenever(writer.notifyListeners(any())).doThrow(RuntimeException("Test exception"))

        val event = ContentValues()
        bufferedWriter.addEvent(event, isUrgent = false)

        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))
        runnableCaptor.firstValue.run()

        // Should not throw exception
        verify(writer).writeEvents(any())
        verify(writer).notifyListeners(any())
    }

    @Test
    fun `multiple addEvent-flush cycles work correctly`() {
        // First cycle
        val event1 = ContentValues().apply { put("key", "value1") }
        bufferedWriter.addEvent(event1, isUrgent = false)
        bufferedWriter.flush()

        // Second cycle
        val event2 = ContentValues().apply { put("key", "value2") }
        bufferedWriter.addEvent(event2, isUrgent = false)
        bufferedWriter.flush()

        // Third cycle with multiple events
        val event3 = ContentValues().apply { put("key", "value3") }
        val event4 = ContentValues().apply { put("key", "value4") }
        bufferedWriter.addEvent(event3, isUrgent = false)
        bufferedWriter.addEvent(event4, isUrgent = false)
        bufferedWriter.flush()

        verify(writer, times(3)).writeEvents(eventsCaptor.capture())
        val allCalls = eventsCaptor.allValues
        assertThat(allCalls).hasSize(3)
        assertThat(allCalls[0]).hasSize(1)
        assertThat(allCalls[1]).hasSize(1)
        assertThat(allCalls[2]).hasSize(2)
    }

    @Test
    fun `scheduled task does nothing after flush clears buffer`() {
        val event = ContentValues()

        bufferedWriter.addEvent(event, isUrgent = false)
        verify(executor).executeDelayed(runnableCaptor.capture(), eq(delayMillis))

        // Flush before scheduled task runs
        bufferedWriter.flush()

        // Now run the originally scheduled task
        runnableCaptor.firstValue.run()

        // Should have been called only once (from flush)
        verify(writer, times(1)).writeEvents(any())
    }

    @Test
    fun `addEvent after flush schedules new delayed task`() {
        val event1 = ContentValues()
        bufferedWriter.addEvent(event1, isUrgent = false)
        bufferedWriter.flush()

        val event2 = ContentValues()
        bufferedWriter.addEvent(event2, isUrgent = false)

        // Should schedule again
        verify(executor, times(2)).executeDelayed(any(), eq(delayMillis))
    }

    @Test
    fun `custom delay is used when specified`() {
        val customDelay = 5000L
        val customBufferedWriter = BufferedEventsWriter(
            writer = writer,
            executor = executor,
            delayMillis = customDelay
        )

        val event = ContentValues()
        customBufferedWriter.addEvent(event, isUrgent = false)

        verify(executor).executeDelayed(any(), eq(customDelay))
    }
}
