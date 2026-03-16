package io.appmetrica.analytics.coreutils.internal.buffering

/**
 * Processor for batches of tasks.
 *
 * Implementations define how to process a batch of tasks, such as:
 * - Writing to a database with transaction support
 * - Writing to a file
 * - Sending events to a server
 * - Notifying listeners
 *
 * @param T the type of task to process
 */
fun interface BatchProcessor<T> {

    /**
     * Processes a batch of tasks.
     *
     * This method is called on a background thread when buffered tasks
     * are ready to be processed.
     *
     * @param tasks list of tasks to process
     */
    fun processBatch(tasks: List<T>)
}
