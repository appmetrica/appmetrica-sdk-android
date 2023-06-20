package io.appmetrica.analytics.impl.location

import android.location.Location

internal interface LocationClientApi {

    fun init()

    fun getLocation(): Location?

    fun registerWakelock(wakelock: Any)

    fun removeWakelock(wakelock: Any)

    fun updateTrackingStatusFromClient(value: Boolean)
}
