package io.appmetrica.analytics.remotepermissions.impl.config.service.converter

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.remotepermissions.impl.RemotePermissionsConfigProto
import io.appmetrica.analytics.remotepermissions.impl.config.service.model.ServiceSideRemotePermissionsConfig

internal class RemotePermissionsConfigProtoConverter :
    ProtobufConverter<ServiceSideRemotePermissionsConfig, RemotePermissionsConfigProto> {

    override fun fromModel(value: ServiceSideRemotePermissionsConfig): RemotePermissionsConfigProto =
        RemotePermissionsConfigProto().apply {
            permissions = value.permittedPermissions.map { it.toByteArray() }.toTypedArray()
        }

    override fun toModel(value: RemotePermissionsConfigProto): ServiceSideRemotePermissionsConfig =
        ServiceSideRemotePermissionsConfig(
            value.permissions?.map { String(it) }?.toSet() ?: emptySet()
        )
}
