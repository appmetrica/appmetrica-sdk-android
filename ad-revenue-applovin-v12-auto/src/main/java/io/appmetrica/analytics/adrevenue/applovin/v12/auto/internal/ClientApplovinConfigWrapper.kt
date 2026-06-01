package io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.client.model.ClientApplovinConfig

class ClientApplovinConfigWrapper internal constructor(
    internal val config: ClientApplovinConfig
) {

    override fun toString(): String {
        return "ClientApplovinConfigWrapper(config=$config)"
    }

    companion object {
        internal fun ClientApplovinConfig.toWrapper() =
            ClientApplovinConfigWrapper(this)
    }
}
