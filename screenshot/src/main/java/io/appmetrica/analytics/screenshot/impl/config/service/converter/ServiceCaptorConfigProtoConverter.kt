package io.appmetrica.analytics.screenshot.impl.config.service.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideServiceCaptorConfig

internal class ServiceCaptorConfigProtoConverter :
    Converter<ServiceSideServiceCaptorConfig, ServiceCaptorConfigProto> {

    override fun fromModel(value: ServiceSideServiceCaptorConfig): ServiceCaptorConfigProto {
        return ServiceCaptorConfigProto().also { proto ->
            proto.enabled = value.enabled
            proto.delaySeconds = value.delaySeconds
        }
    }

    override fun toModel(value: ServiceCaptorConfigProto): ServiceSideServiceCaptorConfig {
        return ServiceSideServiceCaptorConfig(
            value.enabled,
            value.delaySeconds,
        )
    }
}
