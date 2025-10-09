package io.appmetrica.analytics.billing.impl.storage

import io.appmetrica.analytics.billinginterface.internal.BillingInfo

data class AutoInappCollectingInfo(
    val billingInfos: List<BillingInfo>,
    val firstInappCheckOccurred: Boolean
)
