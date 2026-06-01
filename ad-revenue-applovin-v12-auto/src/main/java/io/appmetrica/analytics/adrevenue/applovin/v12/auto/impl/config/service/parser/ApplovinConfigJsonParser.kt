package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.parser

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.Constants
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.protobuf.client.AdRevenueApplovinConfigProtobuf
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils.extractFeature
import org.json.JSONObject

internal class ApplovinConfigJsonParser :
    JsonParser<AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig> {

    override fun parse(
        rawData: JSONObject
    ): AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig {
        return AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig().also { proto ->
            proto.enabled = extractFeature(
                rawData,
                Constants.RemoteConfig.FEATURE_NAME,
                Constants.Defaults.DEFAULT_ENABLED
            )
        }
    }
}
