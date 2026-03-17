package io.appmetrica.analytics.impl.referrer.service.cache

import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.impl.referrer.service.provider.ReferrerProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class CachedReferrerProvider(
    private val delegate: ReferrerProvider,
    private val cache: ReferrerCache,
) : ReferrerProvider {
    private val tag = "[CachedReferrerProvider]"

    private var requested = false
    private val listeners = mutableListOf<ReferrerListener>()

    private val lock = Any()

    override val referrerName: String get() = delegate.referrerName

    override fun requestReferrer(listener: ReferrerListener) {
        addOneShotListener(listener)
        startRequestIfNotYet()
    }

    private fun startRequestIfNotYet() {
        synchronized(lock) {
            if (requested) return
            requested = true

            if (cache.hasReferrer()) return
        }

        try {
            DebugLogger.info(
                tag,
                "The $referrerName referrer is not in the ${cache.name} cache yet. Requesting a new referrer",
            )
            delegate.requestReferrer { saveReferrerAndNotifyListeners(it) }
        } catch (e: Throwable) {
            DebugLogger.error(tag, "Failed to request $referrerName referrer", e)
            notifyListeners(ReferrerResult.Failure("Failed to request referrer", e))
        }
    }

    private fun addOneShotListener(listener: ReferrerListener) {
        val result = synchronized(lock) {
            if (cache.hasReferrer()) {
                cache.getReferrerOrNull() ?: ReferrerResult.Failure("Referrer is null")
            } else {
                DebugLogger.info(
                    tag,
                    "The $referrerName referrer was not found in the ${cache.name} cache. Save listener $listener",
                )
                // if there is no referrer, add the listener to the list under the synchronized block.
                listeners.add(listener)
                null
            }
        }

        if (result != null) {
            DebugLogger.info(tag, "The $referrerName referrer was found in the ${cache.name} cache")
            // it should not be under synchronized
            notifyListener(listener, result)
        }
    }

    private fun saveReferrerAndNotifyListeners(result: ReferrerResult) {
        DebugLogger.info(tag, "The $referrerName referrer was saved in the ${cache.name} cache")
        synchronized(lock) {
            cache.saveReferrer(result)
        }

        notifyListeners(result)
    }

    private fun notifyListeners(result: ReferrerResult) {
        DebugLogger.info(tag, "Notify ${listeners.size} listeners about the $referrerName referrer")
        val localListeners = synchronized(lock) {
            // copy listeners and clear
            listeners.toList().also { listeners.clear() }
        }

        for (listener in localListeners) {
            // it should not be under synchronized
            notifyListener(listener, result)
        }
    }

    // it should not be under synchronized
    private fun notifyListener(listener: ReferrerListener, result: ReferrerResult) {
        DebugLogger.info(tag, "Notify listener $listener about the $referrerName referrer")
        listener.onResult(result)
    }
}
