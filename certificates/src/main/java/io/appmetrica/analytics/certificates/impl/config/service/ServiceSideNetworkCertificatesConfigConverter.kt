package io.appmetrica.analytics.certificates.impl.config.service

import io.appmetrica.analytics.certificates.impl.RemoteNetworkCertificatesConfigProto
import io.appmetrica.analytics.certificates.impl.config.service.converter.NetworkCertificatesConfigProtoConverter
import io.appmetrica.analytics.certificates.impl.config.service.model.ServiceSideNetworkCertificatesConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.protobuf.nano.MessageNano

internal class ServiceSideNetworkCertificatesConfigConverter(
    private val protoConverter: NetworkCertificatesConfigProtoConverter =
        NetworkCertificatesConfigProtoConverter()
) : Converter<ServiceSideNetworkCertificatesConfig, ByteArray> {

    private val tag = "[ServiceSideNetworkCertificatesConfigConverter]"

    override fun fromModel(value: ServiceSideNetworkCertificatesConfig): ByteArray {
        return MessageNano.toByteArray(protoConverter.fromModel(value))
    }

    override fun toModel(value: ByteArray): ServiceSideNetworkCertificatesConfig {
        val proto = try {
            RemoteNetworkCertificatesConfigProto.parseFrom(value)
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
            RemoteNetworkCertificatesConfigProto()
        }
        return protoConverter.toModel(proto)
    }
}
