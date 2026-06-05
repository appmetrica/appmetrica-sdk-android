package io.appmetrica.analytics.location.impl

import io.appmetrica.analytics.locationapi.internal.CacheArguments
import io.appmetrica.analytics.locationapi.internal.LocationFilter

internal data class LocationConfig(
    val locationFilter: LocationFilter = LocationFilter(),
    val cacheArguments: CacheArguments = CacheArguments()
) {

    fun buildUpon(locationFilter: LocationFilter): LocationConfig =
        LocationConfig(locationFilter, cacheArguments)

    fun buildUpon(cacheArguments: CacheArguments): LocationConfig =
        LocationConfig(locationFilter, cacheArguments)
}
