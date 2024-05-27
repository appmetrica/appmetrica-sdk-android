package io.appmetrica.analytics.impl.location.toggles

import io.appmetrica.analytics.coreutils.internal.toggle.SimpleThreadSafeToggle
import io.appmetrica.analytics.impl.location.LocationSubscribers
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.WeakHashMap

class WakelocksToggle :
    SimpleThreadSafeToggle(
        initialState = false,
        tag = "[WakelocksToggle]"
    ),
    LocationSubscribers {

    private val subscribers = WeakHashMap<Any, Any?>()

    @Synchronized
    override fun registerWakelock(subscriber: Any) {
        subscribers[subscriber] = null
        DebugLogger.info(tag, "Register location subscriber. Subscribers count = ${subscribers.size}")
        if (subscribers.size == 1) {
            updateState(true)
        }
    }

    @Synchronized
    override fun removeWakelock(subscriber: Any) {
        subscribers.remove(subscriber)
        DebugLogger.info(tag, "Unregister location subscriber. Subscribers count = ${subscribers.size}")
        if (subscribers.isEmpty()) {
            updateState(false)
        }
    }
}
