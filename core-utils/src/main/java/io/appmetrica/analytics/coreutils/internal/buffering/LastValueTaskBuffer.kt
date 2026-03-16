package io.appmetrica.analytics.coreutils.internal.buffering

/**
 * Task buffer that keeps only the last value.
 *
 * This implementation stores only the most recent task, discarding
 * previous values when a new task is added. When [getAndClear] is called,
 * it returns a list with a single element (the last value).
 *
 * Useful for operations where only the final state matters,
 * such as writing a single value to a file.
 *
 * @param T the type of task to buffer
 */
class LastValueTaskBuffer<T> : TaskBuffer<T> {

    private var lastValue: T? = null

    override fun add(task: T) {
        lastValue = task
    }

    override fun getAndClear(): List<T> {
        val value = lastValue
        lastValue = null
        return if (value != null) listOf(value) else emptyList()
    }

    override fun isEmpty(): Boolean = lastValue == null
}
