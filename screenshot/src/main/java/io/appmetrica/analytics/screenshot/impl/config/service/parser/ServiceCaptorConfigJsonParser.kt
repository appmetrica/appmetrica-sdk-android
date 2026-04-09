package io.appmetrica.analytics.screenshot.impl.config.service.parser

import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optBooleanOrNull
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optLongOrNull
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideServiceCaptorConfig
import org.json.JSONObject

internal class ServiceCaptorConfigJsonParser {

    fun parse(rawData: JSONObject): ServiceSideServiceCaptorConfig? {
        val json = rawData.optJSONObject(Constants.RemoteConfig.SERVICE_CAPTOR_CONFIG)
            ?: return null

        val protoWithDefaults = ServiceCaptorConfigProto()

        val enabled = json.optBooleanOrNull(Constants.RemoteConfig.SERVICE_CAPTOR_CONFIG_ENABLED)
            ?: protoWithDefaults.enabled

        val delaySeconds = json.optLongOrNull(Constants.RemoteConfig.SERVICE_CAPTOR_CONFIG_DELAY_SECONDS)
            ?: protoWithDefaults.delaySeconds

        return ServiceSideServiceCaptorConfig(
            enabled,
            delaySeconds
        )
    }
}
