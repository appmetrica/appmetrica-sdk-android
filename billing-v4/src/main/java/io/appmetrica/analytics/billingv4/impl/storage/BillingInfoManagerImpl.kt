package io.appmetrica.analytics.billingv4.impl.storage

import androidx.annotation.WorkerThread
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

private const val TAG = "[BillingStorageImpl]"

class BillingInfoManagerImpl(
    private val storage: BillingInfoStorage
) : BillingInfoManager {

    private var firstInappCheckOccurred: Boolean = storage.isFirstInappCheckOccurred
    private val billingInfos: MutableMap<String, BillingInfo> =
        storage.billingInfo.associateByTo(mutableMapOf(), BillingInfo::sku)

    @WorkerThread
    override fun update(history: Map<String, BillingInfo>) {
        for (billingInfo in history.values) {
            billingInfos[billingInfo.sku] = billingInfo
        }
        YLogger.info(TAG, "updating $billingInfos")
        storage.saveInfo(billingInfos.values.toList(), firstInappCheckOccurred)
    }

    override fun get(sku: String): BillingInfo? {
        return billingInfos[sku]
    }

    override fun markFirstInappCheckOccurred() {
        if (!firstInappCheckOccurred) {
            firstInappCheckOccurred = true
            storage.saveInfo(billingInfos.values.toList(), firstInappCheckOccurred)
        }
    }

    override fun isFirstInappCheckOccurred(): Boolean = firstInappCheckOccurred
}
