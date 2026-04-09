package io.appmetrica.analytics.screenshot.impl.config.service.parser

import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils.extractFeature
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideApiCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideContentObserverCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideServiceCaptorConfig
import org.json.JSONObject

internal class ScreenshotConfigJsonParser(
    private val apiCaptorConfigJsonParser: ApiCaptorConfigJsonParser = ApiCaptorConfigJsonParser(),
    private val serviceCaptorConfigJsonParser: ServiceCaptorConfigJsonParser = ServiceCaptorConfigJsonParser(),
    private val contentObserverCaptorConfigJsonParser: ContentObserverCaptorConfigJsonParser =
        ContentObserverCaptorConfigJsonParser(),
) {

    private val tag = "[ScreenshotConfigJsonParser]"

    fun parse(rawData: JSONObject): ServiceSideScreenshotConfig {
        DebugLogger.info(tag, "Parsing screenshot config $rawData")

        val enabled = extractFeature(
            rawData,
            Constants.RemoteConfig.FEATURE_NAME,
            Constants.Defaults.DEFAULT_FEATURE_STATE
        )

        val json = rawData.optJSONObject(Constants.RemoteConfig.BLOCK_NAME)
            ?: return ServiceSideScreenshotConfig(
                enabled = enabled,
                apiCaptorConfig = ServiceSideApiCaptorConfig(),
                serviceCaptorConfig = ServiceSideServiceCaptorConfig(),
                contentObserverCaptorConfig = ServiceSideContentObserverCaptorConfig()
            )

        val apiCaptorConfig = apiCaptorConfigJsonParser.parse(json)
            ?: ServiceSideApiCaptorConfig()

        val serviceCaptorConfig = serviceCaptorConfigJsonParser.parse(json)
            ?: ServiceSideServiceCaptorConfig()

        val contentObserverCaptorConfig = contentObserverCaptorConfigJsonParser.parse(json)
            ?: ServiceSideContentObserverCaptorConfig()

        return ServiceSideScreenshotConfig(
            enabled = enabled,
            apiCaptorConfig = apiCaptorConfig,
            serviceCaptorConfig = serviceCaptorConfig,
            contentObserverCaptorConfig = contentObserverCaptorConfig
        )
    }
}
