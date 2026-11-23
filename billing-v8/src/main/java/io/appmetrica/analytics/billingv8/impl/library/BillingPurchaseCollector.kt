package io.appmetrica.analytics.billingv8.impl.library

import androidx.annotation.WorkerThread
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.QueryPurchasesParams
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.billinginterface.internal.library.UtilsProvider

internal class BillingPurchaseCollector(
    private val utilsProvider: UtilsProvider,
) {

    @WorkerThread
    fun collect(
        config: BillingConfig,
        billingClient: BillingClient,
        billingLibraryConnectionHolder: BillingLibraryConnectionHolder,
    ) {
        val listeners = listOf(BillingClient.ProductType.INAPP, BillingClient.ProductType.SUBS).associateWith { type ->
            PurchaseHistoryResponseListenerImpl(
                config,
                billingClient,
                utilsProvider,
                type,
                billingLibraryConnectionHolder
            )
        }
        listeners.values.forEach(billingLibraryConnectionHolder::addListener)
        listeners.forEach { (type, listener) ->
            billingLibraryConnectionHolder.startRequest(listener) {
                billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                        .setProductType(type)
                        .build(),
                    listener
                )
            }
        }
    }
}
