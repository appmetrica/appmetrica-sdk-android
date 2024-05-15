package io.appmetrica.analytics.billingv6.impl.update

import androidx.annotation.WorkerThread
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy
import io.appmetrica.analytics.billingv6.impl.TAG
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.logger.internal.DebugLogger
import java.util.concurrent.TimeUnit

class UpdatePolicyImpl(
    private val systemTimeProvider: SystemTimeProvider = SystemTimeProvider()
) : UpdatePolicy {

    @WorkerThread
    override fun getBillingInfoToUpdate(
        config: BillingConfig,
        history: Map<String, BillingInfo>,
        storage: BillingInfoManager
    ): Map<String, BillingInfo> {
        val productsToUpdate = history.filterValues {
            shouldUpdateBillingInfo(config, it, storage)
        }
        DebugLogger.info(TAG, "Products ${productsToUpdate.values.map { it.productId }} should be updated")
        return productsToUpdate
    }

    @WorkerThread
    private fun shouldUpdateBillingInfo(
        config: BillingConfig,
        historyEntry: BillingInfo,
        storage: BillingInfoManager
    ): Boolean {
        val now = systemTimeProvider.currentTimeMillis()
        DebugLogger.info(TAG, "Product from history $historyEntry now=$now $config")
        if (historyEntry.type == ProductType.INAPP && !storage.isFirstInappCheckOccurred) {
            return now - historyEntry.purchaseTime <=
                TimeUnit.SECONDS.toMillis(config.firstCollectingInappMaxAgeSeconds.toLong())
        }
        val storageEntry = storage[historyEntry.productId] ?: return true
        DebugLogger.info(TAG, "Found product in storage $storageEntry")
        if (storageEntry.purchaseToken != historyEntry.purchaseToken) {
            DebugLogger.info(TAG, "Found product in storage has same purchaseToken")
            return true
        }
        return if (historyEntry.type == ProductType.SUBS) {
            now - storageEntry.sendTime >= TimeUnit.SECONDS.toMillis(config.sendFrequencySeconds.toLong())
        } else false
    }
}
