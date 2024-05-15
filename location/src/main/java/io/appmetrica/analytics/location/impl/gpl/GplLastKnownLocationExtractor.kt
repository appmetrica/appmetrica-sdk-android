package io.appmetrica.analytics.location.impl.gpl

import android.content.Context
import android.location.LocationListener
import io.appmetrica.analytics.coreapi.internal.annotations.GeoThread
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractor
import io.appmetrica.analytics.logger.internal.DebugLogger

private const val TAG = "[GplLastKnownLocationExtractor]"

internal class GplLastKnownLocationExtractor(
    private val context: Context,
    private val permissionResolutionStrategy: PermissionResolutionStrategy,
    private val locationListener: LocationListener,
    private val executor: IHandlerExecutor
) : LastKnownLocationExtractor {

    private val factory = GplWrapperFactory()

    @GeoThread
    override fun updateLastKnownLocation() {
        if (permissionResolutionStrategy.hasNecessaryPermissions(context)) {
            try {
                factory.create(context, locationListener, executor).updateLastKnownLocation()
                DebugLogger.info(TAG, "Update last known location")
            } catch (ex: Throwable) {
                DebugLogger.error(TAG, ex, "Could not update last known location")
            }
        } else {
            DebugLogger.info(TAG, "Cannot update last known location: no permissions.")
        }
    }
}
