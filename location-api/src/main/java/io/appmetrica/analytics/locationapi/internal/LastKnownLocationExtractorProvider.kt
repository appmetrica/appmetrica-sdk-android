package io.appmetrica.analytics.locationapi.internal

import android.content.Context
import android.location.LocationListener
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor

interface LastKnownLocationExtractorProvider : Identifiable {

    fun getExtractor(
        context: Context,
        permissionExtractor: PermissionExtractor,
        executor: IHandlerExecutor,
        listener: LocationListener
    ): LastKnownLocationExtractor
}
