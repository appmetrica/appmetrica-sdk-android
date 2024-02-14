package io.appmetrica.analytics.impl.location

import android.content.Context
import io.appmetrica.analytics.impl.location.stub.LocationApiStub
import io.appmetrica.analytics.logger.internal.YLogger

private const val TAG = "[LocationApiProvider]"

internal class LocationApiProvider {

    private val locationClientProvider = LocationClientProvider()

    fun getLocationApi(context: Context): LocationApi {
        val locationClient = locationClientProvider.getLocationClient()
        return if (locationClient == null) {
            YLogger.info(TAG, "Could not load location client. So use LocationApiStub")
            LocationApiStub()
        } else {
            YLogger.info(TAG, "Successfully load location client. So use full implementation")
            LocationApiImpl(context, LocationControllerImpl(), locationClient)
        }
    }
}
