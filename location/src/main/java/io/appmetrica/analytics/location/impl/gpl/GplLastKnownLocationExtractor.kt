package io.appmetrica.analytics.location.impl.gpl

import android.content.Context
import android.location.LocationListener
import io.appmetrica.analytics.coreapi.internal.annotations.GeoThread
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractor
import io.appmetrica.analytics.logger.internal.YLogger

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
                YLogger.info(TAG, "Update last known location")
            } catch (ex: Throwable) {
                YLogger.error(TAG, ex, "Could not update last known location")
            }
        } else {
            YLogger.info(TAG, "Cannot update last known location: no permissions.")
        }
    }
}
