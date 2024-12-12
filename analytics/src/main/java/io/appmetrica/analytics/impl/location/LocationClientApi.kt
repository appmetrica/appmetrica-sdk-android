package io.appmetrica.analytics.impl.location

import android.location.Location
import io.appmetrica.analytics.locationapi.internal.LocationProvider

internal interface LocationClientApi : LocationProvider {

    fun init()

    fun registerWakelock(wakelock: Any)

    fun removeWakelock(wakelock: Any)

    fun updateTrackingStatusFromClient(value: Boolean)

    fun updateLocationFromClient(location: Location?)
}
