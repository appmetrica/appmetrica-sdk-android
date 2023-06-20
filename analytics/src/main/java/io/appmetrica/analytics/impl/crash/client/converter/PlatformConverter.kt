package io.appmetrica.analytics.impl.crash.client.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.plugins.PluginErrorDetails

internal class PlatformConverter : Converter<String, ByteArray> {

    override fun fromModel(input: String): ByteArray {
        return if (PluginErrorDetails.Platform.NATIVE == input) {
            "JVM"
        } else {
            input
        }.toByteArray()
    }

    override fun toModel(nano: ByteArray): String {
        throw UnsupportedOperationException()
    }
}
