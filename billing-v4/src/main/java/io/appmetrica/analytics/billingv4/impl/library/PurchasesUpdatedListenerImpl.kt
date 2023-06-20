package io.appmetrica.analytics.billingv4.impl.library

import androidx.annotation.UiThread
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import io.appmetrica.analytics.billingv4.impl.BillingUtils
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

private const val TAG = "[PurchasesUpdatedListenerImpl]"

internal class PurchasesUpdatedListenerImpl : PurchasesUpdatedListener {

    @UiThread
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        list: List<Purchase>?
    ) {
        YLogger.info(TAG, "onPurchasesUpdated %s", BillingUtils.toString(billingResult))
    }
}
