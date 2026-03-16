package io.appmetrica.analytics.coreutils.internal.buffering

/**
 * Buffer for tasks with different accumulation strategies.
 *
 * Implementations can use different strategies for storing tasks:
 * - Accumulate all tasks (for database operations)
 * - Keep only the last value (for file write operations)
 *
 * @param T the type of task to buffer
 */
interface TaskBuffer<T> {

    /**
     * Adds a task to the buffer.
     *
     * @param task the task to add
     */
    fun add(task: T)

    /**
     * Returns all accumulated tasks and clears the buffer.
     *
     * @return list of tasks to process
     */
    fun getAndClear(): List<T>

    /**
     * Returns true if the buffer is empty.
     */
    fun isEmpty(): Boolean
}
