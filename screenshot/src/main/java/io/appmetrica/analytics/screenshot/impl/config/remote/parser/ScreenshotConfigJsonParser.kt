package io.appmetrica.analytics.screenshot.impl.config.remote.parser

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.ScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto
import org.json.JSONObject

class ScreenshotConfigJsonParser(
    private val apiCaptorConfigJsonParser: ApiCaptorConfigJsonParser = ApiCaptorConfigJsonParser(),
    private val serviceCaptorConfigJsonParser: ServiceCaptorConfigJsonParser = ServiceCaptorConfigJsonParser(),
    private val contentObserverCaptorConfigJsonParser: ContentObserverCaptorConfigJsonParser =
        ContentObserverCaptorConfigJsonParser(),
) {

    private val tag = "[ScreenshotConfigJsonParser]"

    fun parse(rawData: JSONObject): ScreenshotConfigProto {
        DebugLogger.info(tag, "Parsing screenshot config $rawData")
        val json = rawData.optJSONObject(Constants.RemoteConfig.BLOCK_NAME)
            ?: return ScreenshotConfigProto().also {
                it.apiCaptorConfig = ApiCaptorConfigProto()
                it.serviceCaptorConfig = ServiceCaptorConfigProto()
                it.contentObserverCaptorConfig = ContentObserverCaptorConfigProto().also {
                    it.mediaStoreColumnNames = Constants.Defaults.defaultMediaStoreColumnNames
                }
            }

        return ScreenshotConfigProto().also { proto ->
            apiCaptorConfigJsonParser.parse(json)?.also {
                proto.apiCaptorConfig = it
            }
            serviceCaptorConfigJsonParser.parse(json)?.also {
                proto.serviceCaptorConfig = it
            }
            contentObserverCaptorConfigJsonParser.parse(json)?.also {
                proto.contentObserverCaptorConfig = it
            }
        }
    }
}
