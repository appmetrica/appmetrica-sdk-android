package io.appmetrica.analytics.location.impl.system

import android.content.Context
import android.location.LocationListener
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider

internal class SystemLastKnownLocationExtractorProvider(
    private val provider: String,
    private val permissionStrategyProvider: PermissionStrategyProvider,
    override val identifier: String
) : LastKnownLocationExtractorProvider {

    override fun getExtractor(
        context: Context,
        permissionExtractor: PermissionExtractor,
        executor: IHandlerExecutor,
        listener: LocationListener
    ) = SystemLastKnownLocationExtractor(
        context,
        permissionStrategyProvider.getPermissionResolutionStrategy(permissionExtractor),
        listener,
        provider
    )
}
