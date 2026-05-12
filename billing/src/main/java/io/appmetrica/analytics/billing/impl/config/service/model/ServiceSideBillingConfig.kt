package io.appmetrica.analytics.billing.impl.config.service.model

import io.appmetrica.analytics.billing.impl.Constants

internal class ServiceSideBillingConfig(
    val sendFrequencySeconds: Int,
    val firstCollectingInappMaxAgeSeconds: Int,
) {

    constructor() : this(
        Constants.Defaults.DEFAULT_SEND_FREQUENCY_SECONDS,
        Constants.Defaults.DEFAULT_FIRST_COLLECTING_INAPP_MAX_AGE_SECONDS,
    )

    override fun toString(): String {
        return "ServiceSideBillingConfig(" +
            "sendFrequencySeconds=$sendFrequencySeconds, " +
            "firstCollectingInappMaxAgeSeconds=$firstCollectingInappMaxAgeSeconds" +
            ")"
    }
}
