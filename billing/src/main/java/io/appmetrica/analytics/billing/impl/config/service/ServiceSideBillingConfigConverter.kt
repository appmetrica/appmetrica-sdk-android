package io.appmetrica.analytics.billing.impl.config.service

import io.appmetrica.analytics.billing.impl.RemoteBillingConfigProto
import io.appmetrica.analytics.billing.impl.config.service.converter.RemoteBillingConfigProtoConverter
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideRemoteBillingConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.protobuf.nano.MessageNano

internal class ServiceSideBillingConfigConverter(
    private val protoConverter: RemoteBillingConfigProtoConverter = RemoteBillingConfigProtoConverter()
) : Converter<ServiceSideRemoteBillingConfig, ByteArray> {

    private val tag = "[ServiceSideBillingConfigConverter]"

    override fun fromModel(value: ServiceSideRemoteBillingConfig): ByteArray {
        return MessageNano.toByteArray(protoConverter.fromModel(value))
    }

    override fun toModel(value: ByteArray): ServiceSideRemoteBillingConfig {
        val proto = try {
            RemoteBillingConfigProto.parseFrom(value)
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
            RemoteBillingConfigProto()
        }
        return protoConverter.toModel(proto)
    }
}
