package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.client

import android.os.Bundle
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.Constants
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.client.model.ClientApplovinConfig
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal.ClientApplovinConfigWrapper
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal.ClientApplovinConfigWrapper.Companion.toWrapper
import io.appmetrica.analytics.modulesapi.internal.client.BundleToServiceConfigConverter

internal class BundleToClientApplovinConfigConverter :
    BundleToServiceConfigConverter<ClientApplovinConfigWrapper> {

    override fun fromBundle(bundle: Bundle): ClientApplovinConfigWrapper {
        val config = ClientApplovinConfig(
            enabled = bundle.getBoolean(Constants.ServiceConfig.ENABLED, Constants.Defaults.DEFAULT_ENABLED),
        )
        return config.toWrapper()
    }
}
