package io.appmetrica.analytics.impl.preparer

import io.appmetrica.analytics.impl.db.state.converter.EventExtrasConverter
import io.appmetrica.analytics.impl.protobuf.backend.EventProto

class FullExtrasComposer : ExtrasComposer {

    private val converter = EventExtrasConverter()

    override fun getExtras(input: ByteArray?): Array<EventProto.ReportMessage.Session.Event.ExtrasEntry> {
        input?.let { bytes ->
            val extras = converter.toModel(bytes)
            val result = Array(extras.size) { EventProto.ReportMessage.Session.Event.ExtrasEntry() }
            extras.onEachIndexed { index, entry ->
                result[index].apply {
                    key = entry.key.toByteArray()
                    value = entry.value
                }
            }
            return result
        }
        return emptyArray()
    }
}
