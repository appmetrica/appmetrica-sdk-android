package io.appmetrica.analytics.modulesapi.internal.service

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser

abstract class RemoteConfigExtensionConfiguration<S : Any> {

    /**
     * Requests features from remote config
     */
    abstract fun getFeatures(): List<String>

    /**
     * Requests blocks from remote config
     */
    abstract fun getBlocks(): Map<String, Int>

    /**
     * Parses remote module config from raw remote config
     */
    abstract fun getJsonParser(): JsonParser<S>

    /**
     * Converts remote module config to proto for storage
     */
    abstract fun getProtobufConverter(): Converter<S, ByteArray>

    /**
     * Receives remote module config (from getJsonParser) with some additional info
     */
    abstract fun getRemoteConfigUpdateListener(): RemoteConfigUpdateListener<S>
}
