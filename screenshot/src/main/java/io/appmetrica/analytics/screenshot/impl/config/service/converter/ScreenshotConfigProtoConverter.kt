package io.appmetrica.analytics.screenshot.impl.config.service.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.screenshot.impl.RemoteScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.ScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig

internal class ScreenshotConfigProtoConverter(
    private val apiCaptorConfigProtoConverter: ApiCaptorConfigProtoConverter =
        ApiCaptorConfigProtoConverter(),
    private val serviceCaptorConfigProtoConverter: ServiceCaptorConfigProtoConverter =
        ServiceCaptorConfigProtoConverter(),
    private val contentObserverCaptorConfigProtoConverter: ContentObserverCaptorConfigProtoConverter =
        ContentObserverCaptorConfigProtoConverter(),
) : Converter<ServiceSideScreenshotConfig, RemoteScreenshotConfigProto> {

    override fun fromModel(value: ServiceSideScreenshotConfig): RemoteScreenshotConfigProto {
        return RemoteScreenshotConfigProto().also { proto ->
            proto.enabled = value.enabled
            proto.config = ScreenshotConfigProto().also { screenshotProto ->
                screenshotProto.apiCaptorConfig =
                    value.apiCaptorConfig?.let { apiCaptorConfigProtoConverter.fromModel(it) }
                screenshotProto.serviceCaptorConfig =
                    value.serviceCaptorConfig?.let { serviceCaptorConfigProtoConverter.fromModel(it) }
                screenshotProto.contentObserverCaptorConfig =
                    value.contentObserverCaptorConfig?.let { contentObserverCaptorConfigProtoConverter.fromModel(it) }
            }
        }
    }

    override fun toModel(value: RemoteScreenshotConfigProto): ServiceSideScreenshotConfig {
        return ServiceSideScreenshotConfig(
            enabled = value.enabled,
            apiCaptorConfig = value.config?.apiCaptorConfig
                ?.let { apiCaptorConfigProtoConverter.toModel(it) },
            serviceCaptorConfig = value.config?.serviceCaptorConfig
                ?.let { serviceCaptorConfigProtoConverter.toModel(it) },
            contentObserverCaptorConfig = value.config?.contentObserverCaptorConfig
                ?.let { contentObserverCaptorConfigProtoConverter.toModel(it) },
        )
    }
}
