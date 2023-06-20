package io.appmetrica.analytics.location.impl

import io.appmetrica.analytics.locationapi.internal.CacheArguments
import io.appmetrica.analytics.locationapi.internal.LocationFilter

internal class LocationConfig(
    val locationFilter: LocationFilter = LocationFilter(),
    val cacheArguments: CacheArguments = CacheArguments()
) {

    fun buildUpon(locationFilter: LocationFilter): LocationConfig =
        LocationConfig(locationFilter, cacheArguments)

    fun buildUpon(cacheArguments: CacheArguments): LocationConfig =
        LocationConfig(locationFilter, cacheArguments)

    override fun toString(): String {
        return "LocationConfig(locationFilter=$locationFilter, cacheArguments=$cacheArguments)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocationConfig

        if (locationFilter != other.locationFilter) return false
        if (cacheArguments != other.cacheArguments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = locationFilter.hashCode()
        result = 31 * result + cacheArguments.hashCode()
        return result
    }
}
