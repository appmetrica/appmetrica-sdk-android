package io.appmetrica.analytics.location.impl.gpl

import android.Manifest
import android.content.Context
import android.location.LocationListener
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.coreutils.internal.permission.SinglePermissionStrategy
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractor
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider

internal class GplLastKnownLocationExtractorProvider(
    override val identifier: String
) : LastKnownLocationExtractorProvider {

    override fun getExtractor(
        context: Context,
        permissionExtractor: PermissionExtractor,
        executor: IHandlerExecutor,
        listener: LocationListener
    ): LastKnownLocationExtractor =
        GplLastKnownLocationExtractor(
            context,
            SinglePermissionStrategy(permissionExtractor, Manifest.permission.ACCESS_COARSE_LOCATION),
            listener,
            executor
        )
}
