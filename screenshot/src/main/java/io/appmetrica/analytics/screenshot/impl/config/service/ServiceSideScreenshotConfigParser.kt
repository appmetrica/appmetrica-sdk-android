package io.appmetrica.analytics.screenshot.impl.config.service

import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.screenshot.impl.config.service.parser.ScreenshotConfigJsonParser
import io.appmetrica.analytics.screenshot.internal.ServiceSideScreenshotConfigWrapper
import io.appmetrica.analytics.screenshot.internal.ServiceSideScreenshotConfigWrapper.Companion.toWrapper
import org.json.JSONObject

internal class ServiceSideScreenshotConfigParser(
    private val parser: ScreenshotConfigJsonParser = ScreenshotConfigJsonParser(),
) : JsonParser<ServiceSideScreenshotConfigWrapper> {

    private val tag = "[ServiceSideScreenshotConfigParser]"

    override fun parse(rawData: JSONObject): ServiceSideScreenshotConfigWrapper {
        DebugLogger.info(tag, "Parsing remote module config")
        val screenshotConfig = parser.parse(rawData)
        DebugLogger.info(tag, "Remote module config is '$screenshotConfig'")
        return screenshotConfig.toWrapper()
    }
}
