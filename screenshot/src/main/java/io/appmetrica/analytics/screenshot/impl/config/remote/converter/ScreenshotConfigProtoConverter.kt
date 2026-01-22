package io.appmetrica.analytics.screenshot.impl.config.remote.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.screenshot.impl.ScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ScreenshotConfig

internal class ScreenshotConfigProtoConverter(
    private val apiCaptorConfigProtoConverter: ApiCaptorConfigProtoConverter = ApiCaptorConfigProtoConverter(),
    private val serviceCaptorConfigProtoConverter: ServiceCaptorConfigProtoConverter =
        ServiceCaptorConfigProtoConverter(),
    private val contentObserverCaptorConfigProtoConverter: ContentObserverCaptorConfigProtoConverter =
        ContentObserverCaptorConfigProtoConverter(),
) : Converter<ScreenshotConfig, ScreenshotConfigProto> {

    override fun fromModel(value: ScreenshotConfig): ScreenshotConfigProto {
        return ScreenshotConfigProto().also { proto ->
            proto.apiCaptorConfig = value.apiCaptorConfig?.let { apiCaptorConfigProtoConverter.fromModel(it) }
            proto.serviceCaptorConfig =
                value.serviceCaptorConfig?.let { serviceCaptorConfigProtoConverter.fromModel(it) }
            proto.contentObserverCaptorConfig =
                value.contentObserverCaptorConfig?.let { contentObserverCaptorConfigProtoConverter.fromModel(it) }
        }
    }

    override fun toModel(value: ScreenshotConfigProto): ScreenshotConfig {
        return ScreenshotConfig(
            apiCaptorConfig = value.apiCaptorConfig?.let { apiCaptorConfigProtoConverter.toModel(it) },
            serviceCaptorConfig = value.serviceCaptorConfig?.let { serviceCaptorConfigProtoConverter.toModel(it) },
            contentObserverCaptorConfig = value.contentObserverCaptorConfig
                ?.let { contentObserverCaptorConfigProtoConverter.toModel(it) },
        )
    }
}
