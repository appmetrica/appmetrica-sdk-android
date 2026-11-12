package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.startup.ClientReportsQueueBudgetConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

/**
 * Byte budget for [ClientReportsQueueCategory.DATA] items on the CRS in-memory queue.
 * CONTROL and FATAL do not reserve here.
 *
 * @param maxBytesLimit DATA budget; `null` means disabled (unlimited accept, no drops/evicts for size).
 * Default uses [ClientReportsQueueBudgetConfig.DEFAULT] and current heap max.
 */
internal class ClientReportsQueueBudget(
    private val maxBytesLimit: Long? = ClientReportsQueueBudgetConfig.DEFAULT.toByteLimit(),
) {

    private val tag = "[ClientReportsQueueBudget]"
    private val monitor = Any()

    private var currentBytesValue = 0L
    private var droppedCountValue = 0L
    private var droppedBytesValue = 0L

    init {
        if (maxBytesLimit == null) {
            DebugLogger.info(tag, "Budget applied: disabled")
        } else {
            DebugLogger.info(tag, "Budget applied: limitBytes=$maxBytesLimit")
        }
    }

    val enabled: Boolean
        get() = maxBytesLimit != null

    val limitBytes: Long?
        get() = maxBytesLimit

    val currentBytes: Long
        get() = synchronized(monitor) { currentBytesValue }

    val droppedCount: Long
        get() = synchronized(monitor) { droppedCountValue }

    val droppedBytes: Long
        get() = synchronized(monitor) { droppedBytesValue }

    fun wouldFit(weight: Long): Boolean = synchronized(monitor) {
        val limit = maxBytesLimit ?: return true
        weight <= limit && currentBytesValue + weight <= limit
    }

    /**
     * Reserves [weight] and returns a once-token reservation, or `null` if the item must be dropped.
     */
    fun tryAcquire(weight: Long): ClientReportsQueueReservation? {
        if (!tryReserve(weight)) {
            return null
        }
        return ClientReportsQueueReservation(this, weight)
    }

    /**
     * @return false if the caller must not enqueue (does not fit the DATA budget).
     */
    fun tryReserve(weight: Long): Boolean = synchronized(monitor) {
        val limit = maxBytesLimit
        if (limit == null) {
            currentBytesValue += weight
            return true
        }
        if (weight > limit) {
            return false
        }
        if (currentBytesValue + weight > limit) {
            return false
        }
        currentBytesValue += weight
        true
    }

    fun release(weight: Long) = synchronized(monitor) {
        currentBytesValue = (currentBytesValue - weight).coerceAtLeast(0L)
    }

    fun recordDrop(reason: ClientReportsQueueDropReason, weight: Long) = synchronized(monitor) {
        droppedCountValue += 1
        droppedBytesValue += weight
        DebugLogger.warning(
            tag,
            "Drop enqueue: reason=$reason weight=$weight " +
                "current=$currentBytesValue limit=$maxBytesLimit " +
                "droppedCount=$droppedCountValue droppedBytes=$droppedBytesValue"
        )
    }
}
