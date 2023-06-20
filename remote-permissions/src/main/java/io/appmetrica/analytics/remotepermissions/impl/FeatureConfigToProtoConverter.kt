package io.appmetrica.analytics.remotepermissions.impl

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.remotepermissions.impl.protobuf.client.RemotePermissionsProtobuf

class FeatureConfigToProtoConverter :
    ProtobufConverter<FeatureConfig, RemotePermissionsProtobuf.RemotePermissions> {

    override fun fromModel(value: FeatureConfig): RemotePermissionsProtobuf.RemotePermissions =
        RemotePermissionsProtobuf.RemotePermissions().apply {
            permissions = value.permittedPermissions.map { it.toByteArray() }.toTypedArray()
        }

    override fun toModel(value: RemotePermissionsProtobuf.RemotePermissions): FeatureConfig = FeatureConfig(
        value.permissions?.map { String(it) }?.toSet() ?: emptySet()
    )
}
