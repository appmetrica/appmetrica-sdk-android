package io.appmetrica.analytics.impl.location.toggles

import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.coreutils.internal.toggle.SimpleThreadSafeToggle
import io.appmetrica.analytics.impl.location.LocationSubscribers
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
        YLogger.info(tag, "Register location subscriber. Subscribers count = ${subscribers.size}")
        if (subscribers.size == 1) {
            updateState(true)
        }
    }

    @Synchronized
    override fun removeWakelock(subscriber: Any) {
        subscribers.remove(subscriber)
        YLogger.info(tag, "Unregister location subscriber. Subscribers count = ${subscribers.size}")
        if (subscribers.isEmpty()) {
            updateState(false)
        }
    }
}
