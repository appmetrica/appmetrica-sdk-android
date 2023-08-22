package io.appmetrica.analytics.impl.crash.client.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid

class PluginEnvironmentConverter : Converter<Map<String, String>, Array<CrashAndroid.BytesPair>> {

    override fun fromModel(value: Map<String, String>): Array<CrashAndroid.BytesPair> {
        val result = Array(value.size) { CrashAndroid.BytesPair() }
        var index = 0
        value.forEach { entry ->
            result[index].key = entry.key.toByteArray()
            result[index].value = entry.value.toByteArray()
            index++
        }
        return result
    }

    override fun toModel(value: Array<CrashAndroid.BytesPair>): Map<String, String> {
        throw UnsupportedOperationException()
    }
}
