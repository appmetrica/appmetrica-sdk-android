package io.appmetrica.analytics.billingv6.impl

import com.android.billingclient.api.BillingResult

object BillingUtils {

    fun toString(result: BillingResult): String {
        return "${result.responseCode} : ${result.debugMessage}"
    }
}
