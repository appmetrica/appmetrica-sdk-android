package io.appmetrica.analytics.billingv8.impl.library

import androidx.annotation.WorkerThread
import io.appmetrica.analytics.billingv8.impl.Constants.MODULE_TAG
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

// Tracks the callbacks of a single collection round.
internal class BillingLibraryConnectionHolder(
    private val onFinished: (Boolean) -> Unit,
) {

    private val waitingListeners: MutableSet<Any> = mutableSetOf()
    private var closed = false
    private var failed = false

    @WorkerThread
    fun markFailed() {
        failed = true
    }

    @WorkerThread
    fun addListener(listener: Any) {
        if (!closed) {
            waitingListeners.add(listener)
        }
    }

    @WorkerThread
    fun isWaitingFor(listener: Any): Boolean = listener in waitingListeners

    @WorkerThread
    fun removeListener(listener: Any) {
        if (waitingListeners.remove(listener) && waitingListeners.isEmpty()) {
            closed = true
            onFinished(!failed)
        }
    }

    @WorkerThread
    fun cancel() {
        closed = true
        waitingListeners.clear()
    }

    @WorkerThread
    inline fun startRequest(listener: Any, request: () -> Unit) {
        try {
            request()
        } catch (e: Throwable) {
            DebugLogger.error(MODULE_TAG, e, "Failed to start billing request")
            markFailed()
            removeListener(listener)
        }
    }
}
