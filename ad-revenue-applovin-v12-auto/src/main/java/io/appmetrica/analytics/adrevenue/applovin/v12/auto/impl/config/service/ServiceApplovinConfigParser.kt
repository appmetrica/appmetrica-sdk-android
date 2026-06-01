package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.converter.ApplovinConfigProtoConverter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.parser.ApplovinConfigJsonParser
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal.ServiceApplovinConfigWrapper
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal.ServiceApplovinConfigWrapper.Companion.toWrapper
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import org.json.JSONObject

internal class ServiceApplovinConfigParser(
    private val protoConverter: ApplovinConfigProtoConverter = ApplovinConfigProtoConverter(),
    private val jsonParser: ApplovinConfigJsonParser = ApplovinConfigJsonParser(),
) : JsonParser<ServiceApplovinConfigWrapper> {

    override fun parse(rawData: JSONObject): ServiceApplovinConfigWrapper {
        return protoConverter.toModel(jsonParser.parse(rawData)).toWrapper()
    }
}
