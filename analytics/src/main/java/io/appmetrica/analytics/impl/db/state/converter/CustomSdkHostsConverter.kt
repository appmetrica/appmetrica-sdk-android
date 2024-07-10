package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.impl.utils.ProtobufUtils.toArray

internal class CustomSdkHostsConverter :
    Converter<Map<String, List<String>>, Array<StartupStateProtobuf.StartupState.CustomSdkHostsPair>> {

    override fun fromModel(
        value: Map<String, List<String>>
    ): Array<StartupStateProtobuf.StartupState.CustomSdkHostsPair> {
        return value.toArray { entry ->
            StartupStateProtobuf.StartupState.CustomSdkHostsPair().apply {
                key = entry.key
                hosts = entry.value.toTypedArray()
            }
        }
    }

    override fun toModel(
        value: Array<StartupStateProtobuf.StartupState.CustomSdkHostsPair>
    ): Map<String, List<String>> = value.associate { it.key to it.hosts.toList() }
}
