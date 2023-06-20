package io.appmetrica.analytics.coreapi.internal.cache

interface CacheUpdateScheduler {

    fun onStateUpdated()

    fun scheduleUpdateIfNeededNow()
}
