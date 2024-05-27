package io.appmetrica.analytics.billingv6.impl.storage

import androidx.annotation.WorkerThread
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.billingv6.impl.TAG
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class BillingInfoManagerImpl(
    private val storage: BillingInfoStorage
) : BillingInfoManager {

    private var firstInappCheckOccurred: Boolean = storage.isFirstInappCheckOccurred
    private val billingInfos: MutableMap<String, BillingInfo> =
        storage.billingInfo.associateByTo(mutableMapOf(), BillingInfo::productId)

    @WorkerThread
    override fun update(history: Map<String, BillingInfo>) {
        for (billingInfo in history.values) {
            billingInfos[billingInfo.productId] = billingInfo
        }
        DebugLogger.info(TAG, "updating $billingInfos")
        storage.saveInfo(billingInfos.values.toList(), firstInappCheckOccurred)
    }

    @WorkerThread
    override fun get(productId: String): BillingInfo? {
        return billingInfos[productId]
    }

    @WorkerThread
    override fun markFirstInappCheckOccurred() {
        if (!firstInappCheckOccurred) {
            firstInappCheckOccurred = true
            storage.saveInfo(billingInfos.values.toList(), firstInappCheckOccurred)
        }
    }

    @WorkerThread
    override fun isFirstInappCheckOccurred(): Boolean = firstInappCheckOccurred
}
