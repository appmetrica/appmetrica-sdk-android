package io.appmetrica.analytics.impl.location.stub

import android.content.Context
import android.location.LocationListener
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor
import io.appmetrica.analytics.locationapi.internal.LocationReceiver
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProvider

class LocationReceiverProviderStub : LocationReceiverProvider {

    override val identifier: String = "Location receiver stub"

    override fun getLocationReceiver(
        context: Context,
        permissionExtractor: PermissionExtractor,
        executor: IHandlerExecutor,
        listener: LocationListener
    ): LocationReceiver = LocationReceiverStub()
}
