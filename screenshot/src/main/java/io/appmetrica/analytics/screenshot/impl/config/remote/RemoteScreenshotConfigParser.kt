package io.appmetrica.analytics.screenshot.impl.config.remote

import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils.extractFeature
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.config.remote.converter.ScreenshotConfigProtoConverter
import io.appmetrica.analytics.screenshot.impl.config.remote.parser.ScreenshotConfigJsonParser
import io.appmetrica.analytics.screenshot.internal.config.RemoteScreenshotConfig
import org.json.JSONObject

internal class RemoteScreenshotConfigParser(
    private val converter: ScreenshotConfigProtoConverter = ScreenshotConfigProtoConverter(),
    private val parser: ScreenshotConfigJsonParser = ScreenshotConfigJsonParser(),
) : JsonParser<RemoteScreenshotConfig> {

    private val tag = "[RemoteScreenshotConfigParser]"

    override fun parse(rawData: JSONObject): RemoteScreenshotConfig {
        DebugLogger.info(tag, "Parsing remote module config")
        val enabled = extractFeature(
            rawData,
            Constants.RemoteConfig.FEATURE_NAME,
            Constants.Defaults.DEFAULT_FEATURE_STATE
        )
        val config = converter.toModel(parser.parse(rawData))
        val remoteConfig = RemoteScreenshotConfig(
            enabled,
            config
        )
        DebugLogger.info(tag, "Remote module config is '$remoteConfig'")
        return remoteConfig
    }
}
