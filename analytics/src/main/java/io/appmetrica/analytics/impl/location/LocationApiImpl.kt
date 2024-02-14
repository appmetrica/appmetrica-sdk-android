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
import io.appmetrica.analytics.logger.internal.YLogger

private const val TAG = "[LocationServiceApiImpl]"

internal class LocationApiImpl(
    private val context: Context,
    private val locationController: LocationController,
    private val locationClient: LocationClient
) : LocationApi, StartupStateObserver {

    private val locationFlagStrategy = LocationFlagStrategy()
    override val permissionExtractor = SimplePermissionExtractor(
        CompositePermissionStrategy(
            locationFlagStrategy,
            GlobalServiceLocator.getInstance().modulesController.askForPermissionStrategy
        )
    )
    private val modulesController = GlobalServiceLocator.getInstance().modulesController

    init {
        YLogger.info(TAG, "Register observers...")
        locationController.registerObserver(locationFlagStrategy)
        locationController.registerObserver(locationClient)
    }

    override fun getLocation(): Location? = locationClient.location

    override fun registerWakelock(wakelock: Any) {
        YLogger.info(TAG, "Register wakelock: $wakelock")
        locationController.registerWakelock(wakelock)
    }

    override fun removeWakelock(wakelock: Any) {
        YLogger.info(TAG, "Unsubscribe: $wakelock")
        locationController.removeWakelock(wakelock)
    }

    override fun updateTrackingStatusFromClient(value: Boolean) {
        YLogger.info(TAG, "Update tracking status from client with $value")
        locationController.updateTrackingStatusFromClient(value)
    }

    override fun init() {
        YLogger.info(TAG, "Init...init location client")
        locationClient.init(
            context = context,
            permissionExtractor = permissionExtractor,
            executor = GlobalServiceLocator.getInstance().serviceExecutorProvider.moduleExecutor,
            consumers = modulesController.collectLocationConsumers()
        )
        val locationSourcesController = modulesController.chooseLocationSourceController()

        if (locationSourcesController != null) {
            YLogger.info(TAG, "Init...init module location sources controller")
            locationSourcesController.init()
        } else {
            YLogger.info(TAG, "Init...register default location sources")
            locationClient.registerLocationSource(
                locationClient.lastKnownExtractorProviderFactory.gplLastKnownLocationExtractorProvider
            )
            locationClient.registerLocationSource(
                locationClient.lastKnownExtractorProviderFactory.networkLastKnownLocationExtractorProvider
            )
        }

        YLogger.info(TAG, "Init...init location controller")
        locationController.init(modulesController.chooseLocationAppStateControlToggle())
        GlobalServiceLocator.getInstance().startupStateHolder.registerObserver(this)
    }

    override fun registerControllerObserver(locationControllerObserver: LocationControllerObserver) {
        YLogger.info(TAG, "registerControllerObserver: $locationControllerObserver")
        locationController.registerObserver(locationControllerObserver)
    }

    override fun registerSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider) {
        YLogger.info(TAG, "registerSource: $lastKnownLocationExtractorProvider")
        locationClient.registerLocationSource(lastKnownLocationExtractorProvider)
    }

    override fun unregisterSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider) {
        YLogger.info(TAG, "unregisterSource: $lastKnownLocationExtractorProvider")
        locationClient.unregisterLocationSource(lastKnownLocationExtractorProvider)
    }

    override fun registerSource(locationReceiverProvider: LocationReceiverProvider) {
        YLogger.info(TAG, "registerSource: $locationReceiverProvider")
        locationClient.registerLocationSource(locationReceiverProvider)
    }

    override fun unregisterSource(locationReceiverProvider: LocationReceiverProvider) {
        YLogger.info(TAG, "unregisterSource: $locationReceiverProvider")
        locationClient.unregisterLocationSource(locationReceiverProvider)
    }

    override val lastKnownExtractorProviderFactory: LastKnownLocationExtractorProviderFactory =
        locationClient.lastKnownExtractorProviderFactory

    override val locationReceiverProviderFactory: LocationReceiverProviderFactory =
        locationClient.locationReceiverProviderFactory

    override fun onStartupStateChanged(startupState: StartupState) {
        startupState.cacheControl?.lastKnownLocationTtl?.let {
            YLogger.info(TAG, "update cache arguments: $it")
            locationClient.updateCacheArguments(CacheArguments(it, it * 2))
        }
    }

    override fun updateLocationFilter(locationFilter: LocationFilter) {
        YLogger.info(TAG, "updateLocationFilter: $locationFilter")
        locationClient.updateLocationFilter(locationFilter)
    }
}
