package io.appmetrica.analytics.coreapi.internal.lifecycle

import androidx.annotation.AnyThread

interface ActivityLifecycleRegistry {

    @AnyThread
    fun registerListener(
        listener: ActivityLifecycleListener,
        vararg events: ActivityEvent
    )

    @AnyThread
    fun unregisterListener(
        listener: ActivityLifecycleListener,
        vararg events: ActivityEvent
    )
}
