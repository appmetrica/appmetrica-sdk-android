package io.appmetrica.analytics.impl.db

import android.content.ContentValues
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.buffering.AccumulatingTaskBuffer
import io.appmetrica.analytics.coreutils.internal.buffering.BatchProcessor
import io.appmetrica.analytics.coreutils.internal.buffering.DeferredBatchExecutor
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

/**
 * Buffered events writer that optimizes database write operations by delaying writes.
 *
 * When [addEvent] is called, the event is stored in memory and a delayed write task is scheduled.
 * If [addEvent] is called again before the delay expires, events are accumulated in memory
 * but the scheduled task is not cancelled. This guarantees that data will be written to disk
 * no later than [delayMillis] after the first [addEvent] call in a batch.
 *
 * For urgent events, the writer flushes immediately, bypassing the delay.
 *
 * @param writer The EventBatchWriter to write events to
 * @param executor The executor to use for delayed write operations (typically persistenceExecutor)
 * @param delayMillis The delay in milliseconds before writing to the database (default 1000ms)
 */
internal class BufferedEventsWriter(
    private val writer: EventBatchWriter,
    private val executor: IHandlerExecutor,
    private val delayMillis: Long = 1000
) {

    private val tag = "[BufferedEventsWriter]"

    private val deferredExecutor = DeferredBatchExecutor(
        executor = executor,
        buffer = AccumulatingTaskBuffer<ContentValues>(),
        processor = BatchProcessor { events ->
            try {
                DebugLogger.info(tag, "Writing ${events.size} events")
                writer.writeEvents(events)
                writer.notifyListeners(events)
            } catch (e: Exception) {
                DebugLogger.error(tag, e, "Error writing ${events.size} events")
            }
        },
        delayMillis = delayMillis,
        tag = tag
    )

    /**
     * Adds an event to the buffer.
     *
     * The event is stored in memory and a delayed write task is scheduled if not already scheduled.
     * If this method is called multiple times within the delay period, events are accumulated but
     * the scheduled task is not cancelled, ensuring that data will be written no later than
     * [delayMillis] after the first call.
     *
     * For urgent events, all pending events are flushed immediately.
     *
     * @param event The event data to write
     * @param isUrgent If true, flushes all pending events immediately
     */
    fun addEvent(event: ContentValues, isUrgent: Boolean) {
        DebugLogger.info(tag, "addEvent called (urgent=$isUrgent)")
        deferredExecutor.submit(event, urgent = isUrgent)
    }

    /**
     * Forces an immediate write of all pending events to the database.
     *
     * Cancels the scheduled delayed write task and writes immediately.
     * This is useful when you need to ensure data is persisted before shutdown or
     * when entering background.
     */
    fun flush() {
        deferredExecutor.flush()
    }

    /**
     * Schedules an asynchronous flush of all pending events.
     *
     * Unlike [flush], this method does not block and performs the flush
     * on the background thread. There is no guarantee about when the flush
     * will complete.
     */
    fun flushAsync() {
        deferredExecutor.flushAsync()
    }
}
