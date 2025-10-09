package io.appmetrica.analytics.billing.impl.config.service.model

import io.appmetrica.analytics.billing.internal.config.BillingConfig

class ServiceSideBillingConfig(
    val sendFrequencySeconds: Int,
    val firstCollectingInappMaxAgeSeconds: Int,
) {

    constructor() : this(BillingConfig())

    constructor(remote: BillingConfig) : this(
        remote.sendFrequencySeconds,
        remote.firstCollectingInappMaxAgeSeconds
    )

    override fun toString(): String {
        return "ServiceSideBillingConfig(" +
            "sendFrequencySeconds=$sendFrequencySeconds, " +
            "firstCollectingInappMaxAgeSeconds=$firstCollectingInappMaxAgeSeconds" +
            ")"
    }
}
