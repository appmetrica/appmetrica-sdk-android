package io.appmetrica.analytics.screenshot.impl.config.remote.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.screenshot.impl.RemoteScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.model.RemoteScreenshotConfig

class RemoteScreenshotConfigProtoConverter(
    private val screenshotConfigProtoConverter: ScreenshotConfigProtoConverter = ScreenshotConfigProtoConverter(),
) : Converter<RemoteScreenshotConfig, RemoteScreenshotConfigProto> {

    override fun fromModel(value: RemoteScreenshotConfig): RemoteScreenshotConfigProto {
        return RemoteScreenshotConfigProto().also { proto ->
            proto.enabled = value.enabled
            proto.config = value.config?.let { screenshotConfigProtoConverter.fromModel(it) }
        }
    }

    override fun toModel(value: RemoteScreenshotConfigProto): RemoteScreenshotConfig {
        return RemoteScreenshotConfig(
            enabled = value.enabled,
            config = screenshotConfigProtoConverter.toModel(value.config),
        )
    }
}
