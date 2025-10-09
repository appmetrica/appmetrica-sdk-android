package io.appmetrica.analytics.billing.impl.config.service.model

import io.appmetrica.analytics.billing.internal.config.RemoteBillingConfig

class ServiceSideRemoteBillingConfig(
    val enabled: Boolean,
    val config: ServiceSideBillingConfig,
) {

    constructor() : this(RemoteBillingConfig())

    constructor(remote: RemoteBillingConfig) : this(
        remote.enabled,
        ServiceSideBillingConfig(remote.config)
    )

    override fun toString(): String {
        return "ServiceSideRemoteBillingConfig(" +
            "enabled=$enabled, " +
            "config=$config" +
            ")"
    }
}
