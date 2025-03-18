package io.appmetrica.analytics.screenshot.impl.config.remote.parser

import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optBooleanOrNull
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optLongOrNull
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto
import org.json.JSONObject

class ServiceCaptorConfigJsonParser {

    fun parse(rawData: JSONObject): ServiceCaptorConfigProto? {
        val json = rawData.optJSONObject(Constants.RemoteConfig.SERVICE_CAPTOR_CONFIG)
            ?: return null

        return ServiceCaptorConfigProto().also { proto ->
            json.optBooleanOrNull(Constants.RemoteConfig.SERVICE_CAPTOR_CONFIG_ENABLED)?.also {
                proto.enabled = it
            }
            json.optLongOrNull(Constants.RemoteConfig.SERVICE_CAPTOR_CONFIG_DELAY_SECONDS)?.also {
                proto.delaySeconds = it
            }
        }
    }
}
