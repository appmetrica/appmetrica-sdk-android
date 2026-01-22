package io.appmetrica.analytics.impl.location.stub

import android.content.Context
import android.location.LocationListener
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractor
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider

internal class LastKnownExtractorProviderStub : LastKnownLocationExtractorProvider {

    override val identifier: String
        get() = "Last known extractor stub"

    override fun getExtractor(
        context: Context,
        permissionExtractor: PermissionExtractor,
        executor: IHandlerExecutor,
        listener: LocationListener
    ): LastKnownLocationExtractor = LastKnownExtractorStub()
}
