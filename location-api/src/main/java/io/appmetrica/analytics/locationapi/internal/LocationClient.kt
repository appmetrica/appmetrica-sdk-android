package io.appmetrica.analytics.locationapi.internal

import android.content.Context
import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor

interface LocationClient : LocationControllerObserver, LocationProvider {

    val lastKnownExtractorProviderFactory: LastKnownLocationExtractorProviderFactory

    val locationReceiverProviderFactory: LocationReceiverProviderFactory

    fun init(
        context: Context,
        permissionExtractor: PermissionExtractor,
        executor: IHandlerExecutor,
        consumers: List<Consumer<Location?>>
    )

    fun updateCacheArguments(cacheArguments: CacheArguments)

    fun updateLocationFilter(locationFilter: LocationFilter)

    fun registerSystemLocationSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider)

    fun unregisterSystemLocationSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider)

    fun registerSystemLocationSource(locationReceiverProvider: LocationReceiverProvider)

    fun unregisterSystemLocationSource(locationReceiverProvider: LocationReceiverProvider)

    fun updateUserLocation(location: Location?)
}
