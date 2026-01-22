package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit

internal class AutoCollectedDataSubscribersHolder(
    val componentId: ComponentId,
    private val preferencesComponentDbStorage: PreferencesComponentDbStorage
) {

    private val tag = "[AutoCollectedDataSubscribersHolder-${componentId.apiKey}]"

    private val ttlSynchronizationAccuracy = TimeUnit.MINUTES.toMillis(1)
    private val maxTtl = TimeUnit.DAYS.toMillis(7)

    private val timeProvider = SystemTimeProvider()

    private val subscribers: MutableMap<String, Long> =
        preferencesComponentDbStorage.autoCollectedDataSubscribers.apply {
            DebugLogger.info(tag, "Initial subscribers before cleanup expired: $this")
            cleanupExpired()
            DebugLogger.info(tag, "Initial subscribers after cleanup expired: $this")
        }

    fun updateSubscribers(incomingSubscribers: Set<String>): Boolean {
        DebugLogger.info(tag, "Update subscribers: $incomingSubscribers")
        var changedState = false
        var subscribersListChanged = false
        val currentTime = timeProvider.currentTimeMillis()
        incomingSubscribers.forEach {
            val lastVisitTime = subscribers[it] ?: let { incomingSubscriber ->
                DebugLogger.info(tag, "Detected new subscriber: $incomingSubscriber")
                subscribersListChanged = true
                -1
            }
            DebugLogger.info(
                tag,
                "Incoming subscriber: $it; last visit time: $lastVisitTime; current time: $currentTime"
            )
            if (currentTime - lastVisitTime > ttlSynchronizationAccuracy) {
                subscribers[it] = currentTime
                changedState = true
            }
        }

        if (changedState) {
            DebugLogger.info(tag, "Subscribers state changed. Save it. New State: $subscribers")
            subscribers.cleanupExpired()
            preferencesComponentDbStorage.putAutoCollectedDataSubscribers(subscribers)
        }
        return subscribersListChanged
    }

    fun getSubscribers(): Set<String> = subscribers.keys

    private fun MutableMap<String, Long>.cleanupExpired() {
        val currentTime = timeProvider.currentTimeMillis()
        val expiredSubscribers = mutableSetOf<String>()

        forEach { (subscriber, visitTime) ->
            if (visitTime < currentTime - maxTtl) {
                DebugLogger.info(
                    tag,
                    "Subscriber $subscriber is expired. " +
                        "Current time: $currentTime; visit time: $visitTime"
                )
                expiredSubscribers.add(subscriber)
            }
        }

        expiredSubscribers.forEach { subscriber ->
            remove(subscriber)
        }
    }
}
