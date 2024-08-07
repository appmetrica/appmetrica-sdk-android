package io.appmetrica.analytics.impl.location

import android.content.Context
import android.location.Location
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.StartupStateObserver
import io.appmetrica.analytics.impl.permissions.CompositePermissionStrategy
import io.appmetrica.analytics.impl.permissions.LocationFlagStrategy
import io.appmetrica.analytics.impl.permissions.SimplePermissionExtractor
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.locationapi.internal.CacheArguments
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProviderFactory
import io.appmetrica.analytics.locationapi.internal.LocationClient
import io.appmetrica.analytics.locationapi.internal.LocationControllerObserver
import io.appmetrica.analytics.locationapi.internal.LocationFilter
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProvider
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProviderFactory
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class LocationApiImpl(
    private val context: Context,
    private val locationController: LocationController,
    private val locationClient: LocationClient
) : LocationApi, StartupStateObserver {

    private val tag = "[LocationServiceApiImpl]"

    private val locationFlagStrategy = LocationFlagStrategy()
    override val permissionExtractor = SimplePermissionExtractor(
        CompositePermissionStrategy(
            locationFlagStrategy,
            GlobalServiceLocator.getInstance().modulesController.askForPermissionStrategy
        )
    )
    private val modulesController = GlobalServiceLocator.getInstance().modulesController

    init {
        DebugLogger.info(tag, "Register observers...")
        locationController.registerObserver(locationFlagStrategy)
        locationController.registerObserver(locationClient)
    }

    override fun getLocation(): Location? = locationClient.location

    override fun registerWakelock(wakelock: Any) {
        DebugLogger.info(tag, "Register wakelock: $wakelock")
        locationController.registerWakelock(wakelock)
    }

    override fun removeWakelock(wakelock: Any) {
        DebugLogger.info(tag, "Unsubscribe: $wakelock")
        locationController.removeWakelock(wakelock)
    }

    override fun updateTrackingStatusFromClient(value: Boolean) {
        DebugLogger.info(tag, "Update tracking status from client with $value")
        locationController.updateTrackingStatusFromClient(value)
    }

    override fun init() {
        DebugLogger.info(tag, "Init...init location client")
        locationClient.init(
            context = context,
            permissionExtractor = permissionExtractor,
            executor = GlobalServiceLocator.getInstance().serviceExecutorProvider.moduleExecutor,
            consumers = modulesController.collectLocationConsumers()
        )
        val locationSourcesController = modulesController.chooseLocationSourceController()

        if (locationSourcesController != null) {
            DebugLogger.info(tag, "Init...init module location sources controller")
            locationSourcesController.init()
        } else {
            DebugLogger.info(tag, "Init...register default location sources")
            locationClient.registerLocationSource(
                locationClient.lastKnownExtractorProviderFactory.gplLastKnownLocationExtractorProvider
            )
            locationClient.registerLocationSource(
                locationClient.lastKnownExtractorProviderFactory.networkLastKnownLocationExtractorProvider
            )
        }

        DebugLogger.info(tag, "Init...init location controller")
        locationController.init(modulesController.chooseLocationAppStateControlToggle())
        GlobalServiceLocator.getInstance().startupStateHolder.registerObserver(this)
    }

    override fun registerControllerObserver(locationControllerObserver: LocationControllerObserver) {
        DebugLogger.info(tag, "registerControllerObserver: $locationControllerObserver")
        locationController.registerObserver(locationControllerObserver)
    }

    override fun registerSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider) {
        DebugLogger.info(tag, "registerSource: $lastKnownLocationExtractorProvider")
        locationClient.registerLocationSource(lastKnownLocationExtractorProvider)
    }

    override fun unregisterSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider) {
        DebugLogger.info(tag, "unregisterSource: $lastKnownLocationExtractorProvider")
        locationClient.unregisterLocationSource(lastKnownLocationExtractorProvider)
    }

    override fun registerSource(locationReceiverProvider: LocationReceiverProvider) {
        DebugLogger.info(tag, "registerSource: $locationReceiverProvider")
        locationClient.registerLocationSource(locationReceiverProvider)
    }

    override fun unregisterSource(locationReceiverProvider: LocationReceiverProvider) {
        DebugLogger.info(tag, "unregisterSource: $locationReceiverProvider")
        locationClient.unregisterLocationSource(locationReceiverProvider)
    }

    override val lastKnownExtractorProviderFactory: LastKnownLocationExtractorProviderFactory =
        locationClient.lastKnownExtractorProviderFactory

    override val locationReceiverProviderFactory: LocationReceiverProviderFactory =
        locationClient.locationReceiverProviderFactory

    override fun onStartupStateChanged(startupState: StartupState) {
        startupState.cacheControl?.lastKnownLocationTtl?.let {
            DebugLogger.info(tag, "update cache arguments: $it")
            locationClient.updateCacheArguments(CacheArguments(it, it * 2))
        }
    }

    override fun updateLocationFilter(locationFilter: LocationFilter) {
        DebugLogger.info(tag, "updateLocationFilter: $locationFilter")
        locationClient.updateLocationFilter(locationFilter)
    }
}
