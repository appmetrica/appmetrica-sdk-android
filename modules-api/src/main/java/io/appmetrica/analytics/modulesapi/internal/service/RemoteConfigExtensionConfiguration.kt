package io.appmetrica.analytics.modulesapi.internal.service

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser

abstract class RemoteConfigExtensionConfiguration<S : Any> {

    abstract fun getFeatures(): List<String>

    abstract fun getBlocks(): Map<String, Int>

    abstract fun getJsonParser(): JsonParser<S>

    abstract fun getProtobufConverter(): Converter<S, ByteArray>

    abstract fun getRemoteConfigUpdateListener(): RemoteConfigUpdateListener<S>
}
