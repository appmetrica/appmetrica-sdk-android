package io.appmetrica.analytics.billing.internal.config

import io.appmetrica.analytics.billing.impl.BillingConfigProto

class BillingConfig(
    val sendFrequencySeconds: Int,
    val firstCollectingInappMaxAgeSeconds: Int,
) {

    constructor() : this(
        BillingConfigProto().sendFrequencySeconds,
        BillingConfigProto().firstCollectingInappMaxAgeSeconds,
    )

    override fun toString(): String {
        return "BillingConfig(" +
            "sendFrequencySeconds=$sendFrequencySeconds, " +
            "firstCollectingInappMaxAgeSeconds=$firstCollectingInappMaxAgeSeconds" +
            ")"
    }
}
