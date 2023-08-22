package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf

internal class CustomSdkHostsConverter :
    Converter<Map<String, List<String>>, Array<StartupStateProtobuf.StartupState.CustomSdkHostsPair>> {

    override fun fromModel(
        value: Map<String, List<String>>
    ): Array<StartupStateProtobuf.StartupState.CustomSdkHostsPair> {
        val result = Array(value.size) { StartupStateProtobuf.StartupState.CustomSdkHostsPair() }
        value.onEachIndexed { index, entry ->
            result[index].key = entry.key
            result[index].hosts = entry.value.toTypedArray()
        }
        return result
    }

    override fun toModel(
        value: Array<StartupStateProtobuf.StartupState.CustomSdkHostsPair>
    ): Map<String, List<String>> = value.associate { it.key to it.hosts.toList() }
}
