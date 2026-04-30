package io.appmetrica.analytics.adrevenue.other.impl.config.service

import android.os.Bundle
import io.appmetrica.analytics.adrevenue.other.impl.Constants
import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ServiceSideAdRevenueOtherConfigToBundleConverter {

    private val tag = "[ServiceSideAdRevenueOtherConfigToBundleConverter]"

    fun convert(config: ServiceSideAdRevenueOtherConfig?): Bundle? {
        DebugLogger.info(tag, "convert $config")
        if (config == null) {
            return null
        }
        return Bundle().apply {
            putBoolean(Constants.ServiceConfig.ENABLED, config.enabled)
            putBoolean(Constants.ServiceConfig.INCLUDE_SOURCE, config.includeSource)
        }
    }
}
