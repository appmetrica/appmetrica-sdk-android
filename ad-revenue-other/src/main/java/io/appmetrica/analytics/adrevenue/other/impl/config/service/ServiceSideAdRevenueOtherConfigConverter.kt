package io.appmetrica.analytics.adrevenue.other.impl.config.service

import io.appmetrica.analytics.adrevenue.other.impl.AdRevenueOtherConfigProto
import io.appmetrica.analytics.adrevenue.other.impl.config.service.converter.AdRevenueOtherConfigProtoConverter
import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.protobuf.nano.MessageNano

internal class ServiceSideAdRevenueOtherConfigConverter(
    private val protoConverter: AdRevenueOtherConfigProtoConverter = AdRevenueOtherConfigProtoConverter()
) : Converter<ServiceSideAdRevenueOtherConfig, ByteArray> {

    private val tag = "[ServiceSideAdRevenueOtherConfigConverter]"

    override fun fromModel(value: ServiceSideAdRevenueOtherConfig): ByteArray {
        DebugLogger.info(tag, "Called fromModel")
        return MessageNano.toByteArray(protoConverter.fromModel(value))
    }

    override fun toModel(value: ByteArray): ServiceSideAdRevenueOtherConfig {
        DebugLogger.info(tag, "Called toModel")
        val proto = try {
            AdRevenueOtherConfigProto.parseFrom(value)
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
            AdRevenueOtherConfigProto()
        }
        return protoConverter.toModel(proto).also {
            DebugLogger.info(tag, "Converted to model: $it")
        }
    }
}
