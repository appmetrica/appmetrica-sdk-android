package io.appmetrica.analytics.impl.crash.jvm.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.plugins.PluginErrorDetails

internal class PlatformConverter : Converter<String, ByteArray> {

    override fun fromModel(value: String): ByteArray {
        return if (PluginErrorDetails.Platform.NATIVE == value) {
            "JVM"
        } else {
            value
        }.toByteArray()
    }

    override fun toModel(value: ByteArray): String {
        throw UnsupportedOperationException()
    }
}
