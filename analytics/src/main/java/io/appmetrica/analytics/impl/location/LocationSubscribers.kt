package io.appmetrica.analytics.impl.location

interface LocationSubscribers {

    fun registerWakelock(subscriber: Any)

    fun removeWakelock(subscriber: Any)
}
