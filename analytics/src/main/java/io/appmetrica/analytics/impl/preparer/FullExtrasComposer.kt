package io.appmetrica.analytics.impl.preparer

import io.appmetrica.analytics.impl.db.state.converter.EventExtrasConverter
import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.impl.utils.ProtobufUtils.toArray

class FullExtrasComposer : ExtrasComposer {

    private val converter = EventExtrasConverter()

    override fun getExtras(input: ByteArray?): Array<EventProto.ReportMessage.Session.Event.ExtrasEntry> {
        input?.let { bytes ->
            return converter.toModel(bytes).toArray { entry ->
                EventProto.ReportMessage.Session.Event.ExtrasEntry().apply {
                    key = entry.key.toByteArray()
                    value = entry.value
                }
            }
        }
        return emptyArray()
    }
}
