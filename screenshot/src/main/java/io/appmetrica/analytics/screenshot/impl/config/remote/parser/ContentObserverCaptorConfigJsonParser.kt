package io.appmetrica.analytics.screenshot.impl.config.remote.parser

import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optBooleanOrNull
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optLongOrNull
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto
import org.json.JSONObject

class ContentObserverCaptorConfigJsonParser {

    fun parse(rawData: JSONObject): ContentObserverCaptorConfigProto? {
        val json = rawData.optJSONObject(Constants.RemoteConfig.CONTENT_OBSERVER_CAPTOR_CONFIG)
            ?: return null

        return ContentObserverCaptorConfigProto().also { proto ->
            json.optBooleanOrNull(Constants.RemoteConfig.CONTENT_OBSERVER_CAPTOR_CONFIG_ENABLED)?.also {
                proto.enabled = it
            }
            json.optJSONArray(Constants.RemoteConfig.CONTENT_OBSERVER_CAPTOR_CONFIG_MEDIA_STORE_COLUMN_NAMES)
                ?.also { names ->
                    proto.mediaStoreColumnNames =
                        (0 until names.length()).map { names.getString(it) }.toTypedArray()
                }
            json.optLongOrNull(Constants.RemoteConfig.CONTENT_OBSERVER_CAPTOR_CONFIG_DETECT_WINDOW_SECONDS)?.also {
                proto.detectWindowSeconds = it
            }
        }
    }
}
