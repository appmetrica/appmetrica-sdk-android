package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.idsync.impl.protobuf.client.IdSyncProtobuf
import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig
import io.appmetrica.analytics.protobuf.nano.MessageNano

internal class IdSyncConfigToProtoBytesConverter(
    private val protoConverter: IdSyncConfigToProtoConverter
) : Converter<IdSyncConfig, ByteArray> {

    override fun fromModel(value: IdSyncConfig): ByteArray =
        MessageNano.toByteArray(protoConverter.fromModel(value))

    override fun toModel(value: ByteArray): IdSyncConfig =
        protoConverter.toModel(IdSyncProtobuf.IdSyncConfig.parseFrom(value))
}
