package io.appmetrica.analytics.screenshot.impl.config.service.parser

import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optBooleanOrNull
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optLongOrNull
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideContentObserverCaptorConfig
import org.json.JSONObject

internal class ContentObserverCaptorConfigJsonParser {

    fun parse(rawData: JSONObject): ServiceSideContentObserverCaptorConfig? {
        val json = rawData.optJSONObject(Constants.RemoteConfig.CONTENT_OBSERVER_CAPTOR_CONFIG)
            ?: return null

        val protoWithDefaults = ContentObserverCaptorConfigProto()

        val enabled = json.optBooleanOrNull(Constants.RemoteConfig.CONTENT_OBSERVER_CAPTOR_CONFIG_ENABLED)
            ?: protoWithDefaults.enabled

        val mediaStoreColumnNames = json.optJSONArray(
            Constants.RemoteConfig.CONTENT_OBSERVER_CAPTOR_CONFIG_MEDIA_STORE_COLUMN_NAMES
        )?.let { names ->
            (0 until names.length()).map { names.getString(it) }
        } ?: protoWithDefaults.mediaStoreColumnNames.toList()

        val detectWindowSeconds = json.optLongOrNull(
            Constants.RemoteConfig.CONTENT_OBSERVER_CAPTOR_CONFIG_DETECT_WINDOW_SECONDS
        ) ?: protoWithDefaults.detectWindowSeconds

        return ServiceSideContentObserverCaptorConfig(
            enabled = enabled,
            mediaStoreColumnNames = mediaStoreColumnNames,
            detectWindowSeconds = detectWindowSeconds
        )
    }
}
