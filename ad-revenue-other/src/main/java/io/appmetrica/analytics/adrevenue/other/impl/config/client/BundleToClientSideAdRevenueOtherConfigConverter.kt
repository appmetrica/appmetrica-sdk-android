package io.appmetrica.analytics.adrevenue.other.impl.config.client

import android.os.Bundle
import io.appmetrica.analytics.adrevenue.other.impl.Constants
import io.appmetrica.analytics.adrevenue.other.impl.config.client.model.ClientSideAdRevenueOtherConfig
import io.appmetrica.analytics.adrevenue.other.internal.ClientSideAdRevenueOtherConfigWrapper
import io.appmetrica.analytics.adrevenue.other.internal.ClientSideAdRevenueOtherConfigWrapper.Companion.toWrapper
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.BundleToServiceConfigConverter

internal class BundleToClientSideAdRevenueOtherConfigConverter :
    BundleToServiceConfigConverter<ClientSideAdRevenueOtherConfigWrapper> {

    private val tag = "[BundleToClientSideAdRevenueOtherConfigConverter]"

    override fun fromBundle(bundle: Bundle): ClientSideAdRevenueOtherConfigWrapper {
        DebugLogger.info(tag, "Called fromBundle")
        val config = ClientSideAdRevenueOtherConfig(
            enabled = bundle.getBoolean(Constants.ServiceConfig.ENABLED, Constants.Defaults.DEFAULT_ENABLED),
            includeSource = bundle.getBoolean(
                Constants.ServiceConfig.INCLUDE_SOURCE,
                Constants.Defaults.DEFAULT_INCLUDE_SOURCE
            ),
        )
        return config.toWrapper()
    }
}
