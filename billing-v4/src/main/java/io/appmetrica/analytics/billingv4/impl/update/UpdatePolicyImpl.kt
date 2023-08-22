package io.appmetrica.analytics.billingv4.impl.update

import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import java.util.concurrent.TimeUnit

private const val TAG = "[UpdatePolicyImpl]"

class UpdatePolicyImpl(
    private val systemTimeProvider: SystemTimeProvider = SystemTimeProvider()
) : UpdatePolicy {

    override fun getBillingInfoToUpdate(
        config: BillingConfig,
        history: Map<String, BillingInfo>,
        storage: BillingInfoManager
    ): Map<String, BillingInfo> {
        val productsToUpdate = history.filterValues {
            shouldUpdateBillingInfo(config, it, storage)
        }
        YLogger.info(TAG, "Products ${productsToUpdate.values.map { it.sku }} should be updated")
        return productsToUpdate
    }

    private fun shouldUpdateBillingInfo(
        config: BillingConfig,
        historyEntry: BillingInfo,
        storage: BillingInfoManager
    ): Boolean {
        val now = systemTimeProvider.currentTimeMillis()
        if (historyEntry.type == ProductType.INAPP && !storage.isFirstInappCheckOccurred) {
            return now - historyEntry.purchaseTime <=
                TimeUnit.SECONDS.toMillis(config.firstCollectingInappMaxAgeSeconds.toLong())
        }
        val storageEntry = storage[historyEntry.sku] ?: return true
        if (storageEntry.purchaseToken != historyEntry.purchaseToken) {
            return true
        }
        return if (historyEntry.type == ProductType.SUBS) {
            now - storageEntry.sendTime >= TimeUnit.SECONDS.toMillis(config.sendFrequencySeconds.toLong())
        } else false
    }
}
