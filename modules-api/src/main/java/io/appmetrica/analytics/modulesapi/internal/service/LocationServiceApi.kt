package io.appmetrica.analytics.modulesapi.internal.service

import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProviderFactory
import io.appmetrica.analytics.locationapi.internal.LocationControllerObserver
import io.appmetrica.analytics.locationapi.internal.LocationFilter
import io.appmetrica.analytics.locationapi.internal.LocationProvider
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProvider
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProviderFactory

interface LocationServiceApi : LocationProvider {

    val permissionExtractor: PermissionExtractor

    val lastKnownExtractorProviderFactory: LastKnownLocationExtractorProviderFactory

    val locationReceiverProviderFactory: LocationReceiverProviderFactory

    fun registerControllerObserver(locationControllerObserver: LocationControllerObserver)

    fun registerSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider)

    fun unregisterSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider)

    fun registerSource(locationReceiverProvider: LocationReceiverProvider)

    fun unregisterSource(locationReceiverProvider: LocationReceiverProvider)

    fun updateLocationFilter(locationFilter: LocationFilter)
}
