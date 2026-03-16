package io.appmetrica.analytics.coreutils.internal.buffering

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

/**
 * Executes tasks in batches with deferred execution.
 *
 * This executor buffers tasks in memory using the provided [buffer] strategy and
 * schedules a delayed execution task. When [submit] is called, the task is added to
 * the buffer and a delayed execution is scheduled (if not already scheduled).
 *
 * If [submit] is called multiple times within the delay period, tasks are accumulated
 * but the scheduled task is not cancelled, ensuring that data will be processed no later
 * than [delayMillis] after the first [submit] call in a batch.
 *
 * For urgent tasks, all pending tasks are processed immediately, bypassing the delay.
 *
 * @param T the type of task to execute
 * @param executor the executor to use for delayed task execution
 * @param buffer the buffer strategy for accumulating tasks
 * @param processor the processor to handle batches of tasks
 * @param delayMillis the delay in milliseconds before processing tasks (default 1000ms)
 * @param tag optional tag for logging (defaults to class name)
 */
class DeferredBatchExecutor<T>(
    private val executor: IHandlerExecutor,
    private val buffer: TaskBuffer<T>,
    private val processor: BatchProcessor<T>,
    private val delayMillis: Long = DEFAULT_DELAY_MILLIS,
    private val tag: String = "[DeferredBatchExecutor]"
) {

    // All accesses to this field are protected by synchronized(this)
    private var pendingTask: Runnable? = null

    /**
     * Submits a task for deferred execution.
     *
     * The task is added to the buffer and a delayed execution is scheduled
     * if not already scheduled.
     *
     * @param task the task to submit
     * @param urgent if true, processes all pending tasks immediately
     */
    @Synchronized
    fun submit(task: T, urgent: Boolean = false) {
        DebugLogger.info(tag, "submit called (urgent=$urgent)")

        buffer.add(task)

        if (urgent) {
            DebugLogger.info(tag, "Urgent task detected, processing immediately")
            cancelPendingTaskLocked()
            performProcessLocked()
        } else {
            if (pendingTask == null) {
                scheduleProcessingLocked()
            } else {
                DebugLogger.info(tag, "Processing task already scheduled, accumulating task")
            }
        }
    }

    /**
     * Forces immediate processing of all pending tasks.
     *
     * Cancels the scheduled delayed processing task and processes immediately.
     * This is useful when you need to ensure data is persisted before shutdown or
     * when entering background.
     */
    @Synchronized
    fun flush() {
        DebugLogger.info(tag, "flush called")

        cancelPendingTaskLocked()

        if (!buffer.isEmpty()) {
            performProcessLocked()
        } else {
            DebugLogger.info(tag, "No pending tasks to flush")
        }
    }

    /**
     * Schedules an asynchronous flush of all pending tasks.
     *
     * Unlike [flush], this method does not block and performs the flush
     * on the background thread. There is no guarantee about when the flush
     * will complete.
     */
    fun flushAsync() {
        DebugLogger.info(tag, "flushAsync called")
        executor.execute { flush() }
    }

    private fun cancelPendingTaskLocked() {
        pendingTask?.let {
            DebugLogger.info(tag, "Cancelling pending task")
            executor.remove(it)
            pendingTask = null
        }
    }

    private fun scheduleProcessingLocked() {
        val task = Runnable { performProcess() }
        pendingTask = task

        DebugLogger.info(tag, "Scheduling processing task with delay=$delayMillis ms")
        executor.executeDelayed(task, delayMillis)
    }

    /**
     * Performs the actual processing. Must be called on executor thread.
     */
    @Synchronized
    private fun performProcess() {
        performProcessLocked()
    }

    private fun performProcessLocked() {
        if (buffer.isEmpty()) {
            DebugLogger.info(tag, "No pending tasks to process")
            pendingTask = null
            return
        }

        val tasksToProcess = buffer.getAndClear()
        pendingTask = null

        DebugLogger.info(tag, "Processing ${tasksToProcess.size} tasks")

        try {
            processor.processBatch(tasksToProcess)
        } catch (e: Exception) {
            DebugLogger.error(tag, e, "Error processing batch of ${tasksToProcess.size} tasks")
        }
    }

    companion object {
        private const val DEFAULT_DELAY_MILLIS = 1000L
    }
}
