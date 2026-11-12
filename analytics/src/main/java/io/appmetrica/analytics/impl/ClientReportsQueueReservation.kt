package io.appmetrica.analytics.impl

import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Once-token for a successful [ClientReportsQueueBudget] reserve.
 * Release is idempotent and safe to call from wrap finally and sync fallback paths.
 */
internal class ClientReportsQueueReservation(
    private val budget: ClientReportsQueueBudget,
    private val weight: Long,
) {
    private val released = AtomicBoolean(false)

    fun releaseOnce() {
        if (released.compareAndSet(false, true)) {
            budget.release(weight)
        }
    }

    fun <T> wrap(callable: Callable<T>): Callable<T> = Callable {
        try {
            callable.call()
        } finally {
            releaseOnce()
        }
    }
}
