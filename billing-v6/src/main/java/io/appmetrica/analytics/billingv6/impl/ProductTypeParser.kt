package io.appmetrica.analytics.billingv6.impl

import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.billinginterface.internal.ProductType

internal object ProductTypeParser {

    fun parse(type: String): ProductType {
        return when (type) {
            BillingClient.ProductType.INAPP -> ProductType.INAPP
            BillingClient.ProductType.SUBS -> ProductType.SUBS
            else -> ProductType.UNKNOWN
        }
    }
}
