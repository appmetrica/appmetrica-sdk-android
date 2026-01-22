package io.appmetrica.analytics.billingv8.impl.update

import androidx.annotation.WorkerThread
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy
import io.appmetrica.analytics.billingv8.impl.Constants.MODULE_TAG
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit

internal class UpdatePolicyImpl(
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
        DebugLogger.info(MODULE_TAG, "Products ${productsToUpdate.values.map { it.productId }} should be updated")
        return productsToUpdate
    }

    @WorkerThread
    private fun shouldUpdateBillingInfo(
        config: BillingConfig,
        historyEntry: BillingInfo,
        storage: BillingInfoManager
    ): Boolean {
        DebugLogger.info(
            MODULE_TAG,
            "Product from history $historyEntry now=${systemTimeProvider.currentTimeMillis()} $config"
        )
        if (historyEntry.type == ProductType.INAPP && !storage.isFirstInappCheckOccurred) {
            return historyEntry.isNotTooOld(config)
        }
        val storageEntry = storage[historyEntry.productId]
        DebugLogger.info(MODULE_TAG, "Product for $historyEntry from storage $storageEntry")
        if (storageEntry == null || storageEntry.purchaseToken != historyEntry.purchaseToken) {
            DebugLogger.info(
                MODULE_TAG,
                "Product from storage for $historyEntry is null or has different purchaseToken"
            )
            if (historyEntry.isNotTooOld(config) || historyEntry.isSubscription()) {
                DebugLogger.info(MODULE_TAG, "Product $historyEntry is actually not too old or is subscription")
                return true
            } else {
                DebugLogger.info(MODULE_TAG, "Product $historyEntry is inapp purchase and too old. Ignore it")
            }
        }
        return if (storageEntry != null && historyEntry.isSubscription()) {
            storageEntry.shouldResendSubscription(config)
        } else false
    }

    private fun BillingInfo.isSubscription(): Boolean = type == ProductType.SUBS

    private fun BillingInfo.isNotTooOld(config: BillingConfig): Boolean {
        val delta = systemTimeProvider.currentTimeMillis() - purchaseTime
        val threshold = TimeUnit.SECONDS.toMillis(config.firstCollectingInappMaxAgeSeconds.toLong())
        return delta <= threshold
    }

    private fun BillingInfo.shouldResendSubscription(config: BillingConfig): Boolean {
        val delta = systemTimeProvider.currentTimeMillis() - sendTime
        return delta >= TimeUnit.SECONDS.toMillis(config.sendFrequencySeconds.toLong())
    }
}
