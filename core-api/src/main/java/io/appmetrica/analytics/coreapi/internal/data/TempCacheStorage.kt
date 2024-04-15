package io.appmetrica.analytics.coreapi.internal.data

interface TempCacheStorage {

    interface Entry {
        val id: Long
        val scope: String
        val timestamp: Long
        val data: ByteArray
    }

    fun put(scope: String, timestamp: Long, data: ByteArray): Long

    fun get(scope: String): Entry?

    fun get(scope: String, limit: Int): Collection<Entry>

    fun remove(id: Long)

    fun removeOlderThan(scope: String, interval: Long)
}
