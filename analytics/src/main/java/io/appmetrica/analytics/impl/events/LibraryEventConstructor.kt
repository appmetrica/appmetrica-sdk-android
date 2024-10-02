package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.impl.protobuf.backend.EventProto

class LibraryEventConstructor {

    private val systemEventName = "appmetrica_system_event_42"
    private val nullPlaceholder = "null"

    fun constructEvent(
        sender: String?,
        event: String?,
        payload: String?
    ): ModuleEvent {
        return ModuleEvent.newBuilder(EventProto.ReportMessage.Session.Event.EVENT_CLIENT)
            .withName(systemEventName)
            .withAttributes(
                mapOf(
                    "sender" to (sender ?: nullPlaceholder),
                    "event" to (event ?: nullPlaceholder),
                    "payload" to (payload ?: nullPlaceholder),
                )
            )
            .build()
    }
}
