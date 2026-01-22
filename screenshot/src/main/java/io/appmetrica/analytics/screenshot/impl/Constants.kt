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

        val defaultMediaStoreColumnNames = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
        )
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

    internal object ParcelableConfig {

        const val CONFIG = "config"
    }
}
