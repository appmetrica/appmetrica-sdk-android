package io.appmetrica.analytics.locationapi.internal

interface LocationReceiver {

    fun startLocationUpdates()
    fun stopLocationUpdates()
}
