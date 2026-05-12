package io.appmetrica.analytics.billing.impl.config.service.model

import io.appmetrica.analytics.billing.impl.Constants

internal class ServiceSideRemoteBillingConfig(
    val enabled: Boolean,
    val config: ServiceSideBillingConfig,
) {

    constructor() : this(
        Constants.Defaults.DEFAULT_ENABLED,
        ServiceSideBillingConfig(),
    )

    override fun toString(): String {
        return "ServiceSideRemoteBillingConfig(" +
            "enabled=$enabled, " +
            "config=$config" +
            ")"
    }
}
