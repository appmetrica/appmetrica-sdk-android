package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service

import android.os.Bundle
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.Constants
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.model.ServiceApplovinConfig

internal class ServiceApplovinConfigToBundleConverter {

    fun convert(config: ServiceApplovinConfig?): Bundle? {
        if (config == null) {
            return null
        }
        return Bundle().apply {
            putBoolean(Constants.ServiceConfig.ENABLED, config.enabled)
        }
    }
}
