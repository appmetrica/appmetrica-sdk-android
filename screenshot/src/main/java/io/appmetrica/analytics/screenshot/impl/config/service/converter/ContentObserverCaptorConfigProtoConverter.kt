package io.appmetrica.analytics.screenshot.impl.config.service.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideContentObserverCaptorConfig

internal class ContentObserverCaptorConfigProtoConverter :
    Converter<ServiceSideContentObserverCaptorConfig, ContentObserverCaptorConfigProto> {

    override fun fromModel(value: ServiceSideContentObserverCaptorConfig): ContentObserverCaptorConfigProto {
        return ContentObserverCaptorConfigProto().also { proto ->
            proto.enabled = value.enabled
            proto.mediaStoreColumnNames = value.mediaStoreColumnNames.toTypedArray()
            proto.detectWindowSeconds = value.detectWindowSeconds
        }
    }

    override fun toModel(value: ContentObserverCaptorConfigProto): ServiceSideContentObserverCaptorConfig {
        return ServiceSideContentObserverCaptorConfig(
            enabled = value.enabled,
            mediaStoreColumnNames = value.mediaStoreColumnNames.toList(),
            detectWindowSeconds = value.detectWindowSeconds,
        )
    }
}
