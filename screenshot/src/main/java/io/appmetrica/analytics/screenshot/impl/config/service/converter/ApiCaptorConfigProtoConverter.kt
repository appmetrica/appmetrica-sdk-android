package io.appmetrica.analytics.screenshot.impl.config.service.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideApiCaptorConfig

internal class ApiCaptorConfigProtoConverter : Converter<ServiceSideApiCaptorConfig, ApiCaptorConfigProto> {

    override fun fromModel(value: ServiceSideApiCaptorConfig): ApiCaptorConfigProto {
        return ApiCaptorConfigProto().also { proto ->
            proto.enabled = value.enabled
        }
    }

    override fun toModel(value: ApiCaptorConfigProto): ServiceSideApiCaptorConfig {
        return ServiceSideApiCaptorConfig(
            enabled = value.enabled,
        )
    }
}
