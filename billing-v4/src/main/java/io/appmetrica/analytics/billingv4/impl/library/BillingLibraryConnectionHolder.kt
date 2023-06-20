package io.appmetrica.analytics.billingv4.impl.library

import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

private const val TAG = "[BillingLibraryConnectionHolder]"

internal class BillingLibraryConnectionHolder @JvmOverloads constructor(
    private val billingClient: BillingClient,
    private val mainHandler: Handler = Handler(Looper.getMainLooper())
) {

    private val waitingListeners: MutableSet<Any> = mutableSetOf()

    @WorkerThread
    fun addListener(listener: Any) {
        waitingListeners.add(listener)
    }

    @WorkerThread
    fun removeListener(listener: Any) {
        waitingListeners.remove(listener)
        endConnection()
    }

    @WorkerThread
    private fun endConnection() {
        if (waitingListeners.size == 0) {
            YLogger.info(TAG, "endConnection")
            mainHandler.post(object : SafeRunnable() {
                override fun runSafety() {
                    billingClient.endConnection()
                }
            })
        } else {
            YLogger.info(TAG, "Listeners remaining: %d", waitingListeners.size)
        }
    }
}
