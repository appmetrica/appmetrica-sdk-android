package io.appmetrica.analytics.impl.db.storage

internal class TempCachePutTask(
    val scope: String,
    val timestamp: Long,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TempCachePutTask

        if (scope != other.scope) return false
        if (timestamp != other.timestamp) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = scope.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "TempCachePutTask(scope='$scope', timestamp=$timestamp, data=$data)"
    }
}
