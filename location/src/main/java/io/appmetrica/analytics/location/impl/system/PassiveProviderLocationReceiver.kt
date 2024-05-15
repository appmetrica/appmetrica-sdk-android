package io.appmetrica.analytics.location.impl.system

import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.locationapi.internal.LocationReceiver
import io.appmetrica.analytics.logger.internal.DebugLogger
import java.util.concurrent.TimeUnit

private const val TAG = "[PassiveProviderLocationReceiver]"

internal class PassiveProviderLocationReceiver(
    context: Context,
    private val looper: Looper,
    permissionResolutionStrategy: PermissionResolutionStrategy,
    locationListener: LocationListener,
) : SystemLastKnownLocationExtractor(
    context,
    permissionResolutionStrategy,
    locationListener,
    LocationManager.PASSIVE_PROVIDER,
),
    LocationReceiver {

    private val passiveProviderTimeInterval = TimeUnit.SECONDS.toMillis(1)
    private val passiveProviderDistanceInterval = 0f

    @SuppressWarnings("MissingPermission")
    override fun startLocationUpdates() {
        if (permissionResolutionStrategy.hasNecessaryPermissions(context)) {
            SystemServiceUtils.accessSystemServiceByNameSafely<LocationManager, Unit>(
                context,
                Context.LOCATION_SERVICE,
                "request location updates for $provider provider",
                "location manager"
            ) {
                it.requestLocationUpdates(
                    provider,
                    passiveProviderTimeInterval,
                    passiveProviderDistanceInterval,
                    locationListener,
                    looper
                )
            }
            DebugLogger.info(TAG, "Request location updates for passive provider")
        } else {
            DebugLogger.info(TAG, "Permission resolution strategy forbid location updates")
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun stopLocationUpdates() {
        DebugLogger.info(TAG, "Stop location updates for passive provider")
        SystemServiceUtils.accessSystemServiceByNameSafely<LocationManager, Unit>(
            context,
            Context.LOCATION_SERVICE,
            "stop location updates for passive provider",
            "location manager"
        ) { it.removeUpdates(locationListener) }
    }
}
