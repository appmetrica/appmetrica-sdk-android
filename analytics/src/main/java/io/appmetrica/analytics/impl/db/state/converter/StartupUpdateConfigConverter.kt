package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.impl.startup.StartupUpdateConfig

internal class StartupUpdateConfigConverter :
    ProtobufConverter<StartupUpdateConfig, StartupStateProtobuf.StartupState.StartupUpdateConfig> {

    override fun fromModel(value: StartupUpdateConfig): StartupStateProtobuf.StartupState.StartupUpdateConfig =
        StartupStateProtobuf.StartupState.StartupUpdateConfig().apply {
            interval = value.intervalSeconds
        }

    override fun toModel(value: StartupStateProtobuf.StartupState.StartupUpdateConfig): StartupUpdateConfig =
        StartupUpdateConfig(value.interval)
}
