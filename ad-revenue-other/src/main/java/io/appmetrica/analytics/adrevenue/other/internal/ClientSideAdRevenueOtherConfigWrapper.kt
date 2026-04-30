package io.appmetrica.analytics.adrevenue.other.internal

import io.appmetrica.analytics.adrevenue.other.impl.config.client.model.ClientSideAdRevenueOtherConfig

class ClientSideAdRevenueOtherConfigWrapper internal constructor(
    internal val config: ClientSideAdRevenueOtherConfig
) {

    override fun toString(): String {
        return "ClientSideAdRevenueOtherConfigWrapper(config=$config)"
    }

    companion object {
        internal fun ClientSideAdRevenueOtherConfig.toWrapper() =
            ClientSideAdRevenueOtherConfigWrapper(this)
    }
}
