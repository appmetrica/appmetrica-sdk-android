package io.appmetrica.analytics.location.impl.system

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import io.appmetrica.analytics.coreapi.internal.annotations.GeoThread
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractor
import io.appmetrica.analytics.logger.internal.DebugLogger

internal open class SystemLastKnownLocationExtractor(
    protected val context: Context,
    protected val permissionResolutionStrategy: PermissionResolutionStrategy,
    protected val locationListener: LocationListener,
    protected val provider: String,
) : LastKnownLocationExtractor {

    private val tag = "[SystemLastKnownLocationExtractor-$provider]"

    @SuppressLint("MissingPermission")
    @GeoThread
    override fun updateLastKnownLocation() {
        DebugLogger.info(tag, "Update last known location")
        if (permissionResolutionStrategy.hasNecessaryPermissions(context)) {
            SystemServiceUtils.accessSystemServiceByNameSafely<LocationManager, Location?>(
                context,
                Context.LOCATION_SERVICE,
                "getting last known location for provider $provider",
                "location manager"
            ) { it.getLastKnownLocation(provider) }.let { location ->
                DebugLogger.info(tag, "Notify listener with new location = $location")
                location?.let { locationListener.onLocationChanged(it) }
            }
        } else {
            DebugLogger.info(tag, "Permission resolution strategy forbid updating last known location")
        }
    }
}
