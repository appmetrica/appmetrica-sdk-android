package io.appmetrica.analytics.impl.location

import android.content.Context
import io.appmetrica.analytics.impl.location.stub.LocationApiStub
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class LocationApiProvider {

    private val tag = "[LocationApiProvider]"

    private val locationClientProvider = LocationClientProvider()

    fun getLocationApi(context: Context): LocationApi {
        val locationClient = locationClientProvider.getLocationClient()
        return if (locationClient == null) {
            DebugLogger.info(tag, "Could not load location client. So use LocationApiStub")
            LocationApiStub()
        } else {
            DebugLogger.info(tag, "Successfully load location client. So use full implementation")
            LocationApiImpl(context, LocationControllerImpl(), locationClient)
        }
    }
}
