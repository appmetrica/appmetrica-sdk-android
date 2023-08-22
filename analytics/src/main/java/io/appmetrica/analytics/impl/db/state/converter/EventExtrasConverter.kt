package io.appmetrica.analytics.impl.db.state.converter

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.protobuf.client.EventExtrasProto
import io.appmetrica.analytics.protobuf.nano.MessageNano

class EventExtrasConverter : Converter<Map<String, ByteArray>, ByteArray> {

    override fun fromModel(value: Map<String, ByteArray>): ByteArray {
        val proto = EventExtrasProto.EventExtras()
        proto.extras = Array(value.size) { EventExtrasProto.EventExtras.ExtrasEntry() }
        value.onEachIndexed { index, entry ->
            proto.extras[index].key = entry.key
            proto.extras[index].value = entry.value
        }
        return MessageNano.toByteArray(proto)
    }

    override fun toModel(value: ByteArray): Map<String, ByteArray> =
        EventExtrasProto.EventExtras.parseFrom(value).extras.associate { it.key to it.value }
}
