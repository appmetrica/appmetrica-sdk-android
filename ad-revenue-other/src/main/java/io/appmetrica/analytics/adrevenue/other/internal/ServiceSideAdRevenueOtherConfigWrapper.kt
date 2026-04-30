package io.appmetrica.analytics.adrevenue.other.internal

import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig

class ServiceSideAdRevenueOtherConfigWrapper internal constructor(
    internal val config: ServiceSideAdRevenueOtherConfig
) {

    override fun toString(): String {
        return "ServiceSideAdRevenueOtherConfigWrapper(config=$config)"
    }

    companion object {
        internal fun ServiceSideAdRevenueOtherConfig.toWrapper() =
            ServiceSideAdRevenueOtherConfigWrapper(this)
    }
}
