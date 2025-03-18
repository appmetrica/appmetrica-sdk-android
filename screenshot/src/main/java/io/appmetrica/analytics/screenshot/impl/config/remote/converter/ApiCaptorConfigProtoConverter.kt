package io.appmetrica.analytics.screenshot.impl.config.remote.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ApiCaptorConfig

class ApiCaptorConfigProtoConverter : Converter<ApiCaptorConfig, ApiCaptorConfigProto> {

    override fun fromModel(value: ApiCaptorConfig): ApiCaptorConfigProto {
        return ApiCaptorConfigProto().also { proto ->
            proto.enabled = value.enabled
        }
    }

    override fun toModel(value: ApiCaptorConfigProto): ApiCaptorConfig {
        return ApiCaptorConfig(
            enabled = value.enabled,
        )
    }
}
