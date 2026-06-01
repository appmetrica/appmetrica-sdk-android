package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.converter.ApplovinConfigProtoConverter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.protobuf.client.AdRevenueApplovinConfigProtobuf
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal.ServiceApplovinConfigWrapper
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal.ServiceApplovinConfigWrapper.Companion.toWrapper
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.protobuf.nano.MessageNano

internal class ServiceApplovinConfigConverter(
    private val protoConverter: ApplovinConfigProtoConverter = ApplovinConfigProtoConverter()
) : Converter<ServiceApplovinConfigWrapper, ByteArray> {

    override fun fromModel(value: ServiceApplovinConfigWrapper): ByteArray =
        MessageNano.toByteArray(protoConverter.fromModel(value.config))

    override fun toModel(value: ByteArray): ServiceApplovinConfigWrapper {
        val proto = AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig.parseFrom(value)
        return protoConverter.toModel(proto).toWrapper()
    }
}
