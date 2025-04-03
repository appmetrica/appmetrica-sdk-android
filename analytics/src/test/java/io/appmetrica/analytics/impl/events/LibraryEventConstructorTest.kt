package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.ModuleEvent.Category
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LibraryEventConstructorTest : CommonTest() {

    private val sender = "sender_value"
    private val event = "event_value"
    private val payload = "payload_value"

    private val constructor = LibraryEventConstructor()

    @Test
    fun constructEvent() {
        val moduleEvent = constructor.constructEvent(sender, event, payload)

        ObjectPropertyAssertions(moduleEvent)
            .withPrivateFields(true)
            .checkField("type", EventProto.ReportMessage.Session.Event.EVENT_CLIENT)
            .checkField("name", "appmetrica_system_event_42")
            .checkFieldIsNull("value")
            .checkField("serviceDataReporterType", 1)
            .checkField("category", Category.GENERAL)
            .checkFieldIsNull("environment")
            .checkFieldIsNull("extras")
            .checkFieldRecursively<List<Map.Entry<String, Any>>>("attributes") {
                assertThat(it.actual).containsExactlyInAnyOrderElementsOf(mapOf(
                    "sender" to sender,
                    "event" to event,
                    "payload" to payload,
                ).entries.toList())
            }
            .checkAll()
    }

    @Test
    fun `constructEvent for null values`() {
        val moduleEvent = constructor.constructEvent(null, null, null)

        ObjectPropertyAssertions(moduleEvent)
            .withPrivateFields(true)
            .checkField("type", EventProto.ReportMessage.Session.Event.EVENT_CLIENT)
            .checkField("name", "appmetrica_system_event_42")
            .checkFieldIsNull("value")
            .checkField("serviceDataReporterType", 1)
            .checkField("category", Category.GENERAL)
            .checkFieldIsNull("environment")
            .checkFieldIsNull("extras")
            .checkFieldRecursively<List<Map.Entry<String, Any>>>("attributes") {
                assertThat(it.actual).containsExactlyInAnyOrderElementsOf(mapOf(
                    "sender" to "null",
                    "event" to "null",
                    "payload" to "null",
                ).entries.toList())
            }
            .checkAll()
    }
}
