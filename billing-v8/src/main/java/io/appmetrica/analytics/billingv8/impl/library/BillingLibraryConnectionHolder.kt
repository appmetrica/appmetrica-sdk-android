package io.appmetrica.analytics.billingv8.impl.library

import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.billingv8.impl.MODULE_TAG
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class BillingLibraryConnectionHolder(
    private val billingClient: BillingClient
) {

    private val waitingListeners: MutableSet<Any> = mutableSetOf()

    @WorkerThread
    fun addListener(listener: Any) {
        waitingListeners.add(listener)
    }

    @WorkerThread
    fun removeListener(listener: Any) {
        waitingListeners.remove(listener)
        endConnectionIfNeeded()
    }

    @WorkerThread
    private fun endConnectionIfNeeded() {
        if (waitingListeners.size == 0) {
            DebugLogger.info(MODULE_TAG, "endConnection")
            billingClient.endConnection()
        } else {
            DebugLogger.info(MODULE_TAG, "Listeners remaining: %d", waitingListeners.size)
        }
    }
}
