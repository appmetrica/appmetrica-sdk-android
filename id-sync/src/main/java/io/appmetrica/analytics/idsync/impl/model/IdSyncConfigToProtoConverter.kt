package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.idsync.impl.protobuf.client.IdSyncProtobuf
import io.appmetrica.analytics.idsync.internal.model.IdSyncConfig

internal class IdSyncConfigToProtoConverter : ProtobufConverter<IdSyncConfig, IdSyncProtobuf.IdSyncConfig> {

    private val requestConverter = RequestConfigToProtoConverter()

    override fun fromModel(value: IdSyncConfig): IdSyncProtobuf.IdSyncConfig = IdSyncProtobuf.IdSyncConfig().apply {
        enabled = value.enabled
        requestConfig = IdSyncProtobuf.IdSyncConfig.RequestConfig().apply {
            launchDelay = value.launchDelay
            requests = Array(value.requests.size) { requestConverter.fromModel(value.requests[it]) }
        }
    }

    override fun toModel(value: IdSyncProtobuf.IdSyncConfig): IdSyncConfig {
        val requestConfigProto = value.requestConfig ?: IdSyncProtobuf.IdSyncConfig.RequestConfig()
        return IdSyncConfig(
            enabled = value.enabled,
            launchDelay = requestConfigProto.launchDelay,
            requests = requestConfigProto.requests.map { requestConverter.toModel(it) }
        )
    }
}
