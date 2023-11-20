package io.appmetrica.analytics.billingv6.impl.library

import androidx.annotation.UiThread
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import io.appmetrica.analytics.billingv6.impl.BillingUtils
import io.appmetrica.analytics.billingv6.impl.TAG
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

internal class PurchasesUpdatedListenerImpl : PurchasesUpdatedListener {

    @UiThread
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        list: List<Purchase>?
    ) {
        YLogger.info(TAG, "onPurchasesUpdated %s", BillingUtils.toString(billingResult))
    }
}
