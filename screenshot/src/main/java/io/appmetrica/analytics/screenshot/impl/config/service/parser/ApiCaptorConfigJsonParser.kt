package io.appmetrica.analytics.screenshot.impl.config.service.parser

import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optBooleanOrNull
import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideApiCaptorConfig
import org.json.JSONObject

internal class ApiCaptorConfigJsonParser {

    fun parse(rawData: JSONObject): ServiceSideApiCaptorConfig? {
        val json = rawData.optJSONObject(Constants.RemoteConfig.API_CAPTOR_CONFIG)
            ?: return null

        val protoWithDefaults = ApiCaptorConfigProto()

        val enabled = json.optBooleanOrNull(Constants.RemoteConfig.API_CAPTOR_CONFIG_ENABLED)
            ?: protoWithDefaults.enabled

        return ServiceSideApiCaptorConfig(
            enabled = enabled
        )
    }
}
