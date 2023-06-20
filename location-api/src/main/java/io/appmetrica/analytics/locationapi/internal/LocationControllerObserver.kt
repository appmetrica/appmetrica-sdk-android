package io.appmetrica.analytics.locationapi.internal

interface LocationControllerObserver {

    fun startLocationTracking()

    fun stopLocationTracking()
}
