package io.appmetrica.analytics.impl.location.stub

import android.location.Location
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.impl.location.LocationApi
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProviderFactory
import io.appmetrica.analytics.locationapi.internal.LocationControllerObserver
import io.appmetrica.analytics.locationapi.internal.LocationFilter
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProvider
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProviderFactory

internal class LocationApiStub : LocationApi {

    override val systemLocation: Location? = null

    override val userLocation: Location? = null

    override val permissionExtractor: PermissionExtractor = PermissionExtractorStub()

    override val lastKnownExtractorProviderFactory: LastKnownLocationExtractorProviderFactory =
        LastKnownExtractorProviderFactoryStub()

    override val locationReceiverProviderFactory: LocationReceiverProviderFactory =
        LocationReceiverProviderFactoryStub()

    override fun init() {
        // Do nothing
    }

    override fun updateLocationFromClient(location: Location?) {
        // Do nothing
    }

    override fun registerWakelock(wakelock: Any) {
        // Do nothing
    }

    override fun removeWakelock(wakelock: Any) {
        // Do nothing
    }

    override fun updateTrackingStatusFromClient(value: Boolean) {
        // Do nothing
    }

    override fun registerControllerObserver(locationControllerObserver: LocationControllerObserver) {
        // Do nothing
    }

    override fun registerSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider) {
        // Do nothing
    }

    override fun unregisterSource(lastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider) {
        // Do nothing
    }

    override fun registerSource(locationReceiverProvider: LocationReceiverProvider) {
        // Do nothing
    }

    override fun unregisterSource(locationReceiverProvider: LocationReceiverProvider) {
        // Do nothing
    }

    override fun updateLocationFilter(locationFilter: LocationFilter) {
        // Do nothing
    }
}
