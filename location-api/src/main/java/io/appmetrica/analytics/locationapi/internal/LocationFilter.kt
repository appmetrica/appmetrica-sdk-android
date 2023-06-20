package io.appmetrica.analytics.locationapi.internal

class LocationFilter(
    val updateTimeInterval: Long = 5000L,
    val updateDistanceInterval: Float = 10f
) {

    override fun toString(): String {
        return "LocationFilter(updateTimeInterval=$updateTimeInterval, updateDistanceInterval=$updateDistanceInterval)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocationFilter

        if (updateTimeInterval != other.updateTimeInterval) return false
        if (updateDistanceInterval != other.updateDistanceInterval) return false

        return true
    }

    override fun hashCode(): Int {
        var result = updateTimeInterval.hashCode()
        result = 31 * result + updateDistanceInterval.hashCode()
        return result
    }
}
