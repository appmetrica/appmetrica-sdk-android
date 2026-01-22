package io.appmetrica.analytics.screenshot.impl.config.remote.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ServiceCaptorConfig

internal class ServiceCaptorConfigProtoConverter : Converter<ServiceCaptorConfig, ServiceCaptorConfigProto> {

    override fun fromModel(value: ServiceCaptorConfig): ServiceCaptorConfigProto {
        return ServiceCaptorConfigProto().also { proto ->
            proto.enabled = value.enabled
            proto.delaySeconds = value.delaySeconds
        }
    }

    override fun toModel(value: ServiceCaptorConfigProto): ServiceCaptorConfig {
        return ServiceCaptorConfig(
            value.enabled,
            value.delaySeconds,
        )
    }
}
