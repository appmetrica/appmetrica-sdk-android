package io.appmetrica.analytics.impl.location

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.locationapi.internal.LocationControllerObserver

internal interface LocationController {

    fun init(outerAppStateToggle: Toggle?)

    fun registerObserver(observer: LocationControllerObserver, sticky: Boolean = true)

    fun registerWakelock(wakelock: Any)

    fun removeWakelock(wakelock: Any)

    fun updateTrackingStatusFromClient(value: Boolean)
}
