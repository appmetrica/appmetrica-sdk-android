package io.appmetrica.analytics.location.internal

import android.content.Context
import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.location.impl.LastKnownLocationExtractorProviderFactoryImpl
import io.appmetrica.analytics.location.impl.LocationConfig
import io.appmetrica.analytics.location.impl.LocationCore
import io.appmetrica.analytics.location.impl.LocationReceiverProviderFactoryImpl
import io.appmetrica.analytics.location.impl.LocationStreamDispatcher
import io.appmetrica.analytics.location.impl.system.PassiveLocationReceiverProvider
import io.appmetrica.analytics.locationapi.internal.CacheArguments
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProviderFactory
import io.appmetrica.analytics.locationapi.internal.LocationClient
import io.appmetrica.analytics.locationapi.internal.LocationFilter
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProvider
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProviderFactory

class LocationClientImpl : LocationClient {

    private val tag = "[LocationClientImpl]"

    private val passiveLocationReceiverProvider = PassiveLocationReceiverProvider()
    private var locationCore: LocationCore? = null

    override val lastKnownExtractorProviderFactory: LastKnownLocationExtractorProviderFactory =
        LastKnownLocationExtractorProviderFactoryImpl(passiveLocationReceiverProvider)

    override val locationReceiverProviderFactory: LocationReceiverProviderFactory =
        LocationReceiverProviderFactoryImpl(passiveLocationReceiverProvider)

    private var locationConfig: LocationConfig = LocationConfig()

    @Synchronized
    override fun init(
        context: Context,
        permissionExtractor: PermissionExtractor,
        executor: IHandlerExecutor,
        consumers: List<Consumer<Location?>>
    ) {
        YLogger.info(tag, "init with consumers: %s", consumers)
        if (locationCore == null) {
            YLogger.info(tag, "Create location core...")
            locationCore = LocationCore(
                context,
                permissionExtractor,
                executor,
                LocationStreamDispatcher(consumers, locationConfig)
            )
        } else {
            YLogger.info(tag, "Location core exists... ")
        }
    }

    @Synchronized
    override fun updateCacheArguments(cacheArguments: CacheArguments) {
        YLogger.info(tag, "updateCacheArguments: $cacheArguments")
        locationConfig = locationConfig.buildUpon(cacheArguments)
        locationCore?.updateConfig(locationConfig)
    }

    @Synchronized
    override fun updateLocationFilter(locationFilter: LocationFilter) {
        YLogger.info(tag, "updateLocationFilter: $locationFilter")
        locationConfig = locationConfig.buildUpon(locationFilter)
        locationCore?.updateConfig(locationConfig)
    }

    @Synchronized
    override fun registerLocationSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider) {
        YLogger.info(tag, "registerLocationSource: $lastKnownLocationExtractorProvider")
        locationCore?.registerLastKnownSource(lastKnownLocationExtractorProvider)
    }

    @Synchronized
    override fun unregisterLocationSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider) {
        YLogger.info(tag, "unregisterLocationSource: $lastKnownLocationExtractorProvider")
        locationCore?.unregisterLastKnownSource(lastKnownLocationExtractorProvider)
    }

    @Synchronized
    override fun registerLocationSource(locationReceiverProvider: LocationReceiverProvider) {
        YLogger.info(tag, "registerLocationSource: $locationReceiverProvider")
        locationCore?.registerLocationReceiver(locationReceiverProvider)
    }

    @Synchronized
    override fun unregisterLocationSource(locationReceiverProvider: LocationReceiverProvider) {
        YLogger.info(tag, "unregisterLocationSource: $locationReceiverProvider")
        locationCore?.unregisterLocationReceiver(locationReceiverProvider)
    }

    @Synchronized
    override fun startLocationTracking() {
        YLogger.info(tag, "startLocationTracking")
        locationCore?.startLocationTracking()
    }

    @Synchronized
    override fun stopLocationTracking() {
        YLogger.info(tag, "stopLocationTracking")
        locationCore?.stopLocationTracking()
    }

    override val location: Location?
        @Synchronized
        get() = locationCore?.cachedLocation
}
