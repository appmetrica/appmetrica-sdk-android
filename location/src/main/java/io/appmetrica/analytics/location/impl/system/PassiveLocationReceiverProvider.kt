package io.appmetrica.analytics.location.impl.system

import android.Manifest
import android.content.Context
import android.location.LocationListener
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.coreutils.internal.permission.SinglePermissionStrategy
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProvider

internal class PassiveLocationReceiverProvider : LastKnownLocationExtractorProvider, LocationReceiverProvider {

    override val identifier: String = "location-passive-provider"

    private lateinit var passiveProviderLocationReceiver: PassiveProviderLocationReceiver

    override fun getExtractor(
        context: Context,
        permissionExtractor: PermissionExtractor,
        executor: IHandlerExecutor,
        listener: LocationListener
    ) = getOrCreateReceiver(context, executor, permissionExtractor, listener)

    override fun getLocationReceiver(
        context: Context,
        permissionExtractor: PermissionExtractor,
        executor: IHandlerExecutor,
        listener: LocationListener
    ) = getOrCreateReceiver(context, executor, permissionExtractor, listener)

    @Synchronized
    private fun getOrCreateReceiver(
        context: Context,
        executor: IHandlerExecutor,
        permissionExtractor: PermissionExtractor,
        locationListener: LocationListener
    ): PassiveProviderLocationReceiver {
        if (!this::passiveProviderLocationReceiver.isInitialized) {
            passiveProviderLocationReceiver = PassiveProviderLocationReceiver(
                context,
                executor.looper,
                SinglePermissionStrategy(permissionExtractor, Manifest.permission.ACCESS_FINE_LOCATION),
                locationListener
            )
        }
        return passiveProviderLocationReceiver
    }
}
