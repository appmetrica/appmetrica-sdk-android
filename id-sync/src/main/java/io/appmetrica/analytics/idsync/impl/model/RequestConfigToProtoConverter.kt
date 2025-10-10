package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.idsync.impl.protobuf.client.IdSyncProtobuf
import io.appmetrica.analytics.idsync.internal.model.NetworkType
import io.appmetrica.analytics.idsync.internal.model.Preconditions
import io.appmetrica.analytics.idsync.internal.model.RequestConfig

private typealias RequestProto = IdSyncProtobuf.IdSyncConfig.Request
private typealias PreconditionsProto = IdSyncProtobuf.IdSyncConfig.Preconditions
private typealias HeaderProto = IdSyncProtobuf.IdSyncConfig.Header

internal class RequestConfigToProtoConverter : ProtobufConverter<RequestConfig, RequestProto> {

    override fun fromModel(value: RequestConfig): RequestProto = RequestProto().apply {
        type = value.type.toByteArray()
        preconditions = PreconditionsProto().apply {
            networkType = value.preconditions.networkType.toProto()
        }
        url = value.url.toByteArray()
        headers = value.headers.map { (key, value) ->
            HeaderProto().apply {
                this.name = key.toByteArray()
                this.value = Array(value.size) { value[it].toByteArray() }
            }
        }.toTypedArray()
        resendIntervalForValidResponse = value.resendIntervalForValidResponse
        resendIntervalForInvalidResponse = value.resendIntervalForInvalidResponse
        validResponseCodes = value.validResponseCodes.toIntArray()
    }

    override fun toModel(value: RequestProto): RequestConfig = RequestConfig(
        type = value.type.toString(Charsets.UTF_8),
        preconditions = Preconditions(
            networkType = value.preconditions?.networkType?.networkTypeToModel() ?: NetworkType.ANY
        ),
        url = value.url.toString(Charsets.UTF_8),
        headers = value.headers.associate { proto ->
            proto.name.toString(Charsets.UTF_8) to proto.value.map { it.toString(Charsets.UTF_8) }
        },
        resendIntervalForValidResponse = value.resendIntervalForValidResponse,
        resendIntervalForInvalidResponse = value.resendIntervalForInvalidResponse,
        validResponseCodes = value.validResponseCodes.toList()
    )

    private fun Int.networkTypeToModel(): NetworkType = when (this) {
        IdSyncProtobuf.IdSyncConfig.NETWORK_TYPE_CELL -> NetworkType.CELL
        else -> NetworkType.ANY
    }

    private fun NetworkType.toProto(): Int = when (this) {
        NetworkType.CELL -> IdSyncProtobuf.IdSyncConfig.NETWORK_TYPE_CELL
        else -> IdSyncProtobuf.IdSyncConfig.NETWORK_TYPE_ANY
    }
}
