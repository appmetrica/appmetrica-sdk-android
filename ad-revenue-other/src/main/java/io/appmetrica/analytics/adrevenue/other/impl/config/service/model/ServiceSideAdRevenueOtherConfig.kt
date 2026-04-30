package io.appmetrica.analytics.adrevenue.other.impl.config.service.model

import io.appmetrica.analytics.adrevenue.other.impl.AdRevenueOtherConfigProto

internal class ServiceSideAdRevenueOtherConfig(
    val enabled: Boolean,
    val includeSource: Boolean,
) {
    constructor() : this(
        enabled = AdRevenueOtherConfigProto().enabled,
        includeSource = AdRevenueOtherConfigProto().includeSource,
    )

    override fun toString(): String {
        return "ServiceSideAdRevenueOtherConfig(" +
            "enabled=$enabled" +
            ", includeSource=$includeSource" +
            ")"
    }
}
