package io.appmetrica.analytics.coreutils.internal.buffering

/**
 * Task buffer that merges map entries.
 *
 * This implementation merges all submitted maps into a single map,
 * with later entries overwriting earlier ones for the same key.
 * When [getAndClear] is called, it returns a list with a single merged map.
 *
 * Useful for operations where you want to batch multiple map updates,
 * such as database preference writes where only the final value matters for each key.
 *
 * @param K the type of map keys
 * @param V the type of map values
 */
class MapMergingBuffer<K, V> : TaskBuffer<Map<K, V>> {

    private val merged = mutableMapOf<K, V>()

    override fun add(task: Map<K, V>) {
        merged.putAll(task)
    }

    override fun getAndClear(): List<Map<K, V>> {
        if (merged.isEmpty()) {
            return emptyList()
        }
        val result = merged.toMap()
        merged.clear()
        return listOf(result)
    }

    override fun isEmpty(): Boolean = merged.isEmpty()
}
