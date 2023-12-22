package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.impl.startup.ExternalAttributionConfig

class ExternalAttributionConfigConverter :
    ProtobufConverter<ExternalAttributionConfig, StartupStateProtobuf.StartupState.ExternalAttributionConfig> {

    override fun fromModel(
        value: ExternalAttributionConfig?
    ): StartupStateProtobuf.StartupState.ExternalAttributionConfig {
        val nano = StartupStateProtobuf.StartupState.ExternalAttributionConfig()
        value?.collectingInterval?.let {
            nano.collectingInterval = it
        }

        return nano
    }

    override fun toModel(
        value: StartupStateProtobuf.StartupState.ExternalAttributionConfig
    ): ExternalAttributionConfig {
        return ExternalAttributionConfig(
            value.collectingInterval
        )
    }
}
