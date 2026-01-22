package io.appmetrica.analytics.impl.location

internal interface LocationSubscribers {

    fun registerWakelock(subscriber: Any)

    fun removeWakelock(subscriber: Any)
}
