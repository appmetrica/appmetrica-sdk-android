package io.appmetrica.analytics.locationapi.internal

import android.content.Context
import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor

interface LocationClient : LocationControllerObserver {

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

    fun registerLocationSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider)

    fun unregisterLocationSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider)

    fun registerLocationSource(locationReceiverProvider: LocationReceiverProvider)

    fun unregisterLocationSource(locationReceiverProvider: LocationReceiverProvider)

    val location: Location?
}
