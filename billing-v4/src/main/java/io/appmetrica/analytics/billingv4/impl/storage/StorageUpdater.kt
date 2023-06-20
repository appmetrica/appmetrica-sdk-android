package io.appmetrica.analytics.billingv4.impl.storage

import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider

private const val TAG = "[PurchaseHistoryResponseListenerImpl]"

object StorageUpdater {

    @WorkerThread
    fun updateStorage(
        history: Map<String, BillingInfo>,
        newBillingInfo: Map<String, BillingInfo>,
        type: String,
        billingInfoManager: BillingInfoManager,
        systemTimeProvider: SystemTimeProvider = SystemTimeProvider(),
    ) {
        YLogger.info(TAG, "updating storage")
        val now = systemTimeProvider.currentTimeMillis()
        for (billingInfo in history.values) {
            if (newBillingInfo.containsKey(billingInfo.sku)) {
                billingInfo.sendTime = now
            } else {
                billingInfoManager[billingInfo.sku]?.let {
                    billingInfo.sendTime = it.sendTime
                }
            }
        }
        billingInfoManager.update(history)
        if (!billingInfoManager.isFirstInappCheckOccurred && BillingClient.SkuType.INAPP == type) {
            YLogger.info(TAG, "marking markFirstInappCheckOccurred")
            billingInfoManager.markFirstInappCheckOccurred()
        }
    }
}
