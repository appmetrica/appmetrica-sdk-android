package io.appmetrica.analytics.remotepermissions.impl

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.remotepermissions.impl.protobuf.client.RemotePermissionsProtobuf

class FeatureConfigToProtoBytesConverter : Converter<FeatureConfig, ByteArray> {

    private val featureConfigToProtoConverter = FeatureConfigToProtoConverter()

    override fun fromModel(value: FeatureConfig): ByteArray =
        MessageNano.toByteArray(featureConfigToProtoConverter.fromModel(value))

    override fun toModel(value: ByteArray): FeatureConfig =
        featureConfigToProtoConverter.toModel(RemotePermissionsProtobuf.RemotePermissions.parseFrom(value))
}
