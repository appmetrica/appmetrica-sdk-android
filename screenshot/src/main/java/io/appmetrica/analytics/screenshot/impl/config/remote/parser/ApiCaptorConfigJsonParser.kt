package io.appmetrica.analytics.screenshot.impl.config.remote.parser

import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optBooleanOrNull
import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.Constants
import org.json.JSONObject

internal class ApiCaptorConfigJsonParser {

    fun parse(rawData: JSONObject): ApiCaptorConfigProto? {
        val json = rawData.optJSONObject(Constants.RemoteConfig.API_CAPTOR_CONFIG)
            ?: return null

        return ApiCaptorConfigProto().also { proto ->
            json.optBooleanOrNull(Constants.RemoteConfig.API_CAPTOR_CONFIG_ENABLED)?.also {
                proto.enabled = it
            }
        }
    }
}
