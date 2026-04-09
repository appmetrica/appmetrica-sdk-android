package io.appmetrica.analytics.screenshot.impl

import android.provider.MediaStore
import io.appmetrica.analytics.screenshot.impl.protobuf.client.RemoteScreenshotConfigProtobuf

internal object Constants {

    const val MODULE_ID = "screenshot"

    internal object Events {

        const val NAME = "appmetrica_system_event_screenshot"
        const val TYPE = 4
        const val CAPTOR_TYPE_KEY = "type"
    }

    internal object Defaults {
        private val defaultRemoteScreenshotConfig =
            RemoteScreenshotConfigProtobuf.RemoteScreenshotConfig()
        val DEFAULT_FEATURE_STATE = defaultRemoteScreenshotConfig.enabled

        val defaultMediaStoreColumnNames = listOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
        )

        private val defaultApiCaptorConfig = ApiCaptorConfigProto()
        val DEFAULT_API_CAPTOR_ENABLED = defaultApiCaptorConfig.enabled

        private val defaultServiceCaptorConfig = ServiceCaptorConfigProto()
        val DEFAULT_SERVICE_CAPTOR_ENABLED = defaultServiceCaptorConfig.enabled
        val DEFAULT_SERVICE_CAPTOR_DELAY_SECONDS = defaultServiceCaptorConfig.delaySeconds

        private val defaultContentObserverCaptorConfig = ContentObserverCaptorConfigProto()
        val DEFAULT_CONTENT_OBSERVER_ENABLED = defaultContentObserverCaptorConfig.enabled
        val DEFAULT_CONTENT_OBSERVER_DETECT_WINDOW_SECONDS = defaultContentObserverCaptorConfig.detectWindowSeconds
    }

    internal object RemoteConfig {
        const val BLOCK_NAME = "screenshot"
        const val BLOCK_NAME_OBFUSCATED = "scr"
        const val BLOCK_VERSION = 1

        const val FEATURE_NAME = "screenshot"
        const val FEATURE_NAME_OBFUSCATED = "scr"

        const val SERVICE_CAPTOR_CONFIG = "service_captor_config"
        const val SERVICE_CAPTOR_CONFIG_ENABLED = "enabled"
        const val SERVICE_CAPTOR_CONFIG_DELAY_SECONDS = "delay_seconds"

        const val CONTENT_OBSERVER_CAPTOR_CONFIG = "content_observer_captor_config"
        const val CONTENT_OBSERVER_CAPTOR_CONFIG_ENABLED = "enabled"
        const val CONTENT_OBSERVER_CAPTOR_CONFIG_MEDIA_STORE_COLUMN_NAMES = "media_store_column_names"
        const val CONTENT_OBSERVER_CAPTOR_CONFIG_DETECT_WINDOW_SECONDS = "detect_window_seconds"

        const val API_CAPTOR_CONFIG = "api_captor_config"
        const val API_CAPTOR_CONFIG_ENABLED = "enabled"
    }

    internal object ServiceConfig {
        const val ENABLED = "enabled"
        const val API_CAPTOR_ENABLED = "api_captor_enabled"
        const val SERVICE_CAPTOR_ENABLED = "service_captor_enabled"
        const val SERVICE_CAPTOR_DELAY_SECONDS = "service_captor_delay_seconds"
        const val CONTENT_OBSERVER_ENABLED = "content_observer_enabled"
        const val CONTENT_OBSERVER_MEDIA_STORE_COLUMN_NAMES = "content_observer_media_store_column_names"
        const val CONTENT_OBSERVER_DETECT_WINDOW_SECONDS = "content_observer_detect_window_seconds"
    }
}
