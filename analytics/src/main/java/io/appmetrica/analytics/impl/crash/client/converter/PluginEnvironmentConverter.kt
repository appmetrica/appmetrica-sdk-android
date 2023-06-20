package io.appmetrica.analytics.impl.crash.client.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid

class PluginEnvironmentConverter : Converter<Map<String, String>, Array<CrashAndroid.BytesPair>> {

    override fun fromModel(input: Map<String, String>): Array<CrashAndroid.BytesPair> {
        val result = Array(input.size) { CrashAndroid.BytesPair() }
        var index = 0
        input.forEach { entry ->
            result[index].key = entry.key.toByteArray()
            result[index].value = entry.value.toByteArray()
            index++
        }
        return result
    }

    override fun toModel(nano: Array<CrashAndroid.BytesPair>): Map<String, String> {
        throw UnsupportedOperationException()
    }
}
