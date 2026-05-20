package io.appmetrica.analytics.remotepermissions.impl.config.service

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.remotepermissions.impl.RemotePermissionsConfigProto
import io.appmetrica.analytics.remotepermissions.impl.config.service.converter.RemotePermissionsConfigProtoConverter
import io.appmetrica.analytics.remotepermissions.internal.ServiceSideRemotePermissionsConfigWrapper

internal class ServiceSideRemotePermissionsConfigConverter(
    private val protoConverter: RemotePermissionsConfigProtoConverter = RemotePermissionsConfigProtoConverter()
) : Converter<ServiceSideRemotePermissionsConfigWrapper, ByteArray> {

    override fun fromModel(value: ServiceSideRemotePermissionsConfigWrapper): ByteArray =
        MessageNano.toByteArray(protoConverter.fromModel(value.config))

    override fun toModel(value: ByteArray): ServiceSideRemotePermissionsConfigWrapper {
        val proto = RemotePermissionsConfigProto.parseFrom(value)
        return ServiceSideRemotePermissionsConfigWrapper(protoConverter.toModel(proto))
    }
}
