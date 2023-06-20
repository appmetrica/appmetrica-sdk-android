package io.appmetrica.analytics.location.impl

import android.Manifest
import android.location.LocationManager
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.coreutils.internal.permission.SinglePermissionStrategy
import io.appmetrica.analytics.location.impl.gpl.GplLastKnownLocationExtractorProvider
import io.appmetrica.analytics.location.impl.system.PermissionStrategyProvider
import io.appmetrica.analytics.location.impl.system.SystemLastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProviderFactory

internal class LastKnownLocationExtractorProviderFactoryImpl(
    override val passiveLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider
) : LastKnownLocationExtractorProviderFactory {

    override val gplLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider =
        GplLastKnownLocationExtractorProvider("location-module-gpl")

    override val networkLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider =
        SystemLastKnownLocationExtractorProvider(
            LocationManager.NETWORK_PROVIDER,
            object : PermissionStrategyProvider {

                override fun getPermissionResolutionStrategy(
                    permissionExtractor: PermissionExtractor
                ): PermissionResolutionStrategy =
                    SinglePermissionStrategy(permissionExtractor, Manifest.permission.ACCESS_COARSE_LOCATION)
            },
            "location-module-network"
        )

    override val gpsLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider =
        SystemLastKnownLocationExtractorProvider(
            LocationManager.GPS_PROVIDER,
            object : PermissionStrategyProvider {

                override fun getPermissionResolutionStrategy(
                    permissionExtractor: PermissionExtractor
                ): PermissionResolutionStrategy =
                    SinglePermissionStrategy(permissionExtractor, Manifest.permission.ACCESS_FINE_LOCATION)
            },
            "location-module-gps"
        )
}
