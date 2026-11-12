package io.appmetrica.analytics.impl

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.ArrayDeque
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch

/**
 * CRS in-memory pending FIFO with DATA budget admission/eviction and drain scheduling.
 * CONTROL/FATAL skip DATA reservation; cancellation of a not-yet-started item is atomic under [monitor].
 */
internal class ClientReportsQueue(
    private val queueBudget: ClientReportsQueueBudget,
    private val executor: ICommonExecutor,
) {

    private val tag = "[ClientReportsQueue]"
    private val monitor = Any()
    private val pending = ArrayDeque<ClientReportsQueuePendingItem>()
    private var drainPosted = false

    private val drainRunnable = Runnable { drainOne() }

    /**
     * @return enqueued item, or `null` when a DATA item was dropped without enqueue.
     */
    fun enqueue(
        category: ClientReportsQueueCategory,
        weight: Long,
        callable: Callable<*>,
        completionLatch: CountDownLatch? = null,
    ): ClientReportsQueuePendingItem? {
        return when (category) {
            ClientReportsQueueCategory.DATA -> enqueueData(weight, callable)
            ClientReportsQueueCategory.CONTROL,
            ClientReportsQueueCategory.FATAL,
            -> enqueueWithoutBudget(category, weight, callable, completionLatch)
        }
    }

    /**
     * Removes [item] if it has not started draining yet. Releases DATA reservation if any.
     * @return true when the item was removed before start.
     */
    fun tryCancelIfNotStarted(item: ClientReportsQueuePendingItem): Boolean {
        synchronized(monitor) {
            if (item.started) {
                return false
            }
            if (!pending.remove(item)) {
                return false
            }
            item.reservation?.releaseOnce()
            item.clearRefs()
            return true
        }
    }

    @VisibleForTesting
    internal fun pendingSizeForTests(): Int = synchronized(monitor) { pending.size }

    private fun enqueueData(weight: Long, callable: Callable<*>): ClientReportsQueuePendingItem? {
        synchronized(monitor) {
            if (queueBudget.enabled) {
                val limit = queueBudget.limitBytes
                if (limit != null && weight > limit) {
                    queueBudget.recordDrop(ClientReportsQueueDropReason.EVENT_EXCEEDS_BUDGET, weight)
                    return null
                }
                while (!queueBudget.wouldFit(weight)) {
                    if (!evictOldestPendingDataLocked()) {
                        break
                    }
                }
            }
            val reservation = queueBudget.tryAcquire(weight)
            if (reservation == null) {
                queueBudget.recordDrop(ClientReportsQueueDropReason.QUEUE_BUDGET_EXCEEDED, weight)
                return null
            }
            val item = ClientReportsQueuePendingItem(
                category = ClientReportsQueueCategory.DATA,
                weight = weight,
                callable = callable,
                reservation = reservation,
            )
            pending.addLast(item)
            ensureDrainPostedLocked()
            return item
        }
    }

    private fun enqueueWithoutBudget(
        category: ClientReportsQueueCategory,
        weight: Long,
        callable: Callable<*>,
        completionLatch: CountDownLatch?,
    ): ClientReportsQueuePendingItem {
        synchronized(monitor) {
            val item = ClientReportsQueuePendingItem(
                category = category,
                weight = weight,
                callable = callable,
                completionLatch = completionLatch,
            )
            pending.addLast(item)
            ensureDrainPostedLocked()
            return item
        }
    }

    private fun evictOldestPendingDataLocked(): Boolean {
        val iterator = pending.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.category == ClientReportsQueueCategory.DATA && !item.started) {
                iterator.remove()
                item.reservation?.releaseOnce()
                val weight = item.weight
                item.clearRefs()
                queueBudget.recordDrop(ClientReportsQueueDropReason.EVICTED_OLDEST, weight)
                return true
            }
        }
        return false
    }

    private fun ensureDrainPostedLocked() {
        if (!drainPosted) {
            drainPosted = true
            // Must use post (not execute) to avoid inline reentrancy on CRS.
            executor.post(drainRunnable)
        }
    }

    private fun drainOne() {
        val item = synchronized(monitor) {
            val next = pending.pollFirst()
            if (next == null) {
                drainPosted = false
                return
            }
            next.started = true
            next
        }
        val callable = item.callable
        try {
            callable?.call()
        } catch (t: Throwable) {
            DebugLogger.error(tag, t)
        } finally {
            synchronized(monitor) {
                item.reservation?.releaseOnce()
                item.completionLatch?.countDown()
                item.clearRefs()
                if (pending.isNotEmpty()) {
                    executor.post(drainRunnable)
                } else {
                    drainPosted = false
                }
            }
        }
    }
}
