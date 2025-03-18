package io.appmetrica.analytics.screenshot.impl.config.remote.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ContentObserverCaptorConfig

class ContentObserverCaptorConfigProtoConverter :
    Converter<ContentObserverCaptorConfig, ContentObserverCaptorConfigProto> {

    override fun fromModel(value: ContentObserverCaptorConfig): ContentObserverCaptorConfigProto {
        return ContentObserverCaptorConfigProto().also { proto ->
            proto.enabled = value.enabled
            proto.mediaStoreColumnNames = value.mediaStoreColumnNames.toTypedArray()
            proto.detectWindowSeconds = value.detectWindowSeconds
        }
    }

    override fun toModel(value: ContentObserverCaptorConfigProto): ContentObserverCaptorConfig {
        return ContentObserverCaptorConfig(
            enabled = value.enabled,
            mediaStoreColumnNames = value.mediaStoreColumnNames.toList(),
            detectWindowSeconds = value.detectWindowSeconds,
        )
    }
}
