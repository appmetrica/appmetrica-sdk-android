package io.appmetrica.analytics.billing.internal.config

import io.appmetrica.analytics.billing.impl.RemoteBillingConfigProto

class RemoteBillingConfig(
    val enabled: Boolean,
    val config: BillingConfig,
) {

    constructor() : this(
        RemoteBillingConfigProto().enabled,
        BillingConfig(),
    )

    override fun toString(): String {
        return "RemoteBillingConfig(" +
            "enabled=$enabled, " +
            "config=$config" +
            ")"
    }
}
