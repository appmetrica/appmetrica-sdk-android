package io.appmetrica.analytics.locationapi.internal

import java.util.concurrent.TimeUnit

class CacheArguments(
    val refreshPeriod: Long = TimeUnit.SECONDS.toMillis(10),
    val outdatedTimeInterval: Long = TimeUnit.MINUTES.toMillis(2)
) {

    override fun toString(): String {
        return "CacheArguments(refreshPeriod=$refreshPeriod, outdatedTimeInterval=$outdatedTimeInterval)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CacheArguments

        if (refreshPeriod != other.refreshPeriod) return false
        if (outdatedTimeInterval != other.outdatedTimeInterval) return false

        return true
    }

    override fun hashCode(): Int {
        var result = refreshPeriod.hashCode()
        result = 31 * result + outdatedTimeInterval.hashCode()
        return result
    }
}
