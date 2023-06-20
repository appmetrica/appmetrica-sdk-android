package io.appmetrica.analytics.billingv4.impl

import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.billinginterface.internal.ProductType

object ProductTypeParser {

    fun parse(type: String): ProductType {
        return when (type) {
            BillingClient.SkuType.INAPP -> ProductType.INAPP
            BillingClient.SkuType.SUBS -> ProductType.SUBS
            else -> ProductType.UNKNOWN
        }
    }
}
