package io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.model.ServiceApplovinConfig

class ServiceApplovinConfigWrapper internal constructor(
    internal val config: ServiceApplovinConfig
) {

    override fun toString(): String {
        return "ServiceApplovinConfigWrapper(config=$config)"
    }

    companion object {
        internal fun ServiceApplovinConfig.toWrapper() =
            ServiceApplovinConfigWrapper(this)
    }
}
