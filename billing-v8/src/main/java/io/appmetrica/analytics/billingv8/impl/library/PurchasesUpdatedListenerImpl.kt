package io.appmetrica.analytics.billingv8.impl.library

import androidx.annotation.UiThread
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import io.appmetrica.analytics.billingv8.impl.BillingUtils
import io.appmetrica.analytics.billingv8.impl.MODULE_TAG
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class PurchasesUpdatedListenerImpl : PurchasesUpdatedListener {

    @UiThread
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        list: List<Purchase>?
    ) {
        DebugLogger.info(MODULE_TAG, "onPurchasesUpdated %s", BillingUtils.toString(billingResult))
    }
}
