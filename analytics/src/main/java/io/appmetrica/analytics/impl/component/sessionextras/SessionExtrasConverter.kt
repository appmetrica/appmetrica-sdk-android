package io.appmetrica.analytics.impl.component.sessionextras

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.impl.protobuf.client.SessionExtrasProtobuf

internal class SessionExtrasConverter :
    ProtobufConverter<Map<String, ByteArray>, SessionExtrasProtobuf.SessionExtras> {

    override fun fromModel(value: Map<String, ByteArray>): SessionExtrasProtobuf.SessionExtras =
        SessionExtrasProtobuf.SessionExtras().apply {
            extras = value.map { entry ->
                SessionExtrasProtobuf.SessionStateExtrasEntry().apply {
                    key = entry.key.toByteArray()
                    this.value = entry.value
                }
            }.toTypedArray()
        }

    override fun toModel(value: SessionExtrasProtobuf.SessionExtras): Map<String, ByteArray> =
        value.extras.associate { String(it.key) to it.value }
}
