package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage

class TempCacheEntry(
    override val id: Long,
    override val scope: String,
    override val timestamp: Long,
    override val data: ByteArray
) : TempCacheStorage.Entry {

    override fun toString(): String {
        return "TempCacheEntry(id=$id, scope='$scope', timestamp=$timestamp, data=array[${data.size}])"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TempCacheEntry

        if (id != other.id) return false
        if (scope != other.scope) return false
        if (timestamp != other.timestamp) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + scope.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
