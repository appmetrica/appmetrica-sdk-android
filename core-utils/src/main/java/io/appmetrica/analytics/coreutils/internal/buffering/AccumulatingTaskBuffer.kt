package io.appmetrica.analytics.coreutils.internal.buffering

/**
 * Task buffer that accumulates all tasks.
 *
 * This implementation stores all added tasks in a list and returns
 * them all when [getAndClear] is called.
 *
 * Useful for operations where all tasks need to be processed,
 * such as database batch inserts.
 *
 * @param T the type of task to buffer
 */
class AccumulatingTaskBuffer<T> : TaskBuffer<T> {

    private val tasks = mutableListOf<T>()

    override fun add(task: T) {
        tasks.add(task)
    }

    override fun getAndClear(): List<T> {
        if (tasks.isEmpty()) {
            return emptyList()
        }
        val result = tasks.toList()
        tasks.clear()
        return result
    }

    override fun isEmpty(): Boolean = tasks.isEmpty()
}
