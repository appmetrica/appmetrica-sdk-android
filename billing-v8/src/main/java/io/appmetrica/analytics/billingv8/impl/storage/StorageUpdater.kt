package io.appmetrica.analytics.billingv8.impl.storage

import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billingv8.impl.MODULE_TAG
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

object StorageUpdater {

    @WorkerThread
    fun updateStorage(
        history: Map<String, BillingInfo>,
        newBillingInfo: Map<String, BillingInfo>,
        type: String,
        billingInfoManager: BillingInfoManager,
        systemTimeProvider: SystemTimeProvider = SystemTimeProvider(),
    ) {
        DebugLogger.info(MODULE_TAG, "updating storage")
        val now = systemTimeProvider.currentTimeMillis()
        for (billingInfo in history.values) {
            if (newBillingInfo.containsKey(billingInfo.productId)) {
                billingInfo.sendTime = now
            } else {
                billingInfoManager[billingInfo.productId]?.let {
                    billingInfo.sendTime = it.sendTime
                }
            }
        }
        billingInfoManager.update(history)
        if (!billingInfoManager.isFirstInappCheckOccurred && BillingClient.ProductType.INAPP == type) {
            DebugLogger.info(MODULE_TAG, "marking markFirstInappCheckOccurred")
            billingInfoManager.markFirstInappCheckOccurred()
        }
    }
}
