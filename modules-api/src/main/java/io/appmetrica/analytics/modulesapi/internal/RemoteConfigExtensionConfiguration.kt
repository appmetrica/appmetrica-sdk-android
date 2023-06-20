package io.appmetrica.analytics.modulesapi.internal

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser

interface RemoteConfigExtensionConfiguration<S : Any> {

    fun getFeatures(): List<String>

    fun getBlocks(): Map<String, Int>

    fun getJsonParser(): JsonParser<S>

    fun getProtobufConverter(): Converter<S, ByteArray>

    fun getRemoteConfigUpdateListener(): RemoteConfigUpdateListener<S>
}
