package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.modulesapi.internal.RemoteConfigExtensionConfiguration

internal class ModuleRemoteConfigController(
    val configuration: RemoteConfigExtensionConfiguration<Any>
) : JsonParser<Any> by configuration.getJsonParser(),
    Converter<Any, ByteArray> by configuration.getProtobufConverter()
