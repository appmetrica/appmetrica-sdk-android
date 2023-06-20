package io.appmetrica.analytics.impl.permissions

import android.Manifest
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.locationapi.internal.LocationControllerObserver

internal class LocationFlagStrategy : PermissionStrategy, LocationControllerObserver {

    @Volatile
    private var enabled = false

    companion object {

        private const val TAG = "[LocationFlagStrategy]"

        private val locationPermissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun forbidUsePermission(permission: String): Boolean {
        return if (locationPermissions.contains(permission)) {
            YLogger.info(TAG, "forbidUsePermission %s = %s", permission, !enabled)
            return !enabled
        } else {
            false
        }
    }

    override fun startLocationTracking() {
        enabled = true
    }

    override fun stopLocationTracking() {
        enabled = false
    }

    override fun toString(): String {
        return "LocationFlagStrategy(enabled=$enabled, locationPermissions=$locationPermissions)"
    }
}
