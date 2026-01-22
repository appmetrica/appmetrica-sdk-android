package io.appmetrica.analytics.billing.impl.config.remote

import io.appmetrica.analytics.billing.impl.RemoteBillingConfigProto
import io.appmetrica.analytics.billing.impl.config.remote.converter.RemoteBillingConfigProtoConverter
import io.appmetrica.analytics.billing.internal.config.RemoteBillingConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.protobuf.nano.MessageNano

internal class RemoteBillingConfigConverter(
    private val protoConverter: RemoteBillingConfigProtoConverter = RemoteBillingConfigProtoConverter()
) : Converter<RemoteBillingConfig, ByteArray> {

    private val tag = "[RemoteBillingConfigConverter]"

    override fun fromModel(value: RemoteBillingConfig): ByteArray {
        return MessageNano.toByteArray(protoConverter.fromModel(value))
    }

    override fun toModel(value: ByteArray): RemoteBillingConfig {
        val proto = try {
            RemoteBillingConfigProto.parseFrom(value)
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
            RemoteBillingConfigProto()
        }
        return protoConverter.toModel(proto)
    }
}
