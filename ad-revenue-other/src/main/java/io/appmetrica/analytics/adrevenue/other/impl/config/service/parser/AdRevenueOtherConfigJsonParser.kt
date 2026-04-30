package io.appmetrica.analytics.adrevenue.other.impl.config.service.parser

import io.appmetrica.analytics.adrevenue.other.impl.Constants
import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils.extractFeature
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

internal class AdRevenueOtherConfigJsonParser {

    private val tag = "[AdRevenueOtherConfigJsonParser]"

    fun parse(rawData: JSONObject): ServiceSideAdRevenueOtherConfig {
        DebugLogger.info(tag, "Parsing ad revenue other config $rawData")

        val enabled = extractFeature(
            rawData,
            Constants.RemoteConfig.FEATURE_NAME,
            Constants.Defaults.DEFAULT_ENABLED
        )
        val includeSource = extractFeature(
            rawData,
            Constants.RemoteConfig.INCLUDE_SOURCE_NAME,
            Constants.Defaults.DEFAULT_INCLUDE_SOURCE
        )
        return ServiceSideAdRevenueOtherConfig(enabled, includeSource)
    }
}
