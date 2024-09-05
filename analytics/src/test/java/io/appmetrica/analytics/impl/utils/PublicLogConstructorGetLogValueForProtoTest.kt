package io.appmetrica.analytics.impl.utils

import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class PublicLogConstructorGetLogValueForProtoTest(
    private val eventType: Int,
    private val expectedValue: String
) : CommonTest() {

    companion object {
        private const val MESSAGE = "Message"
        private const val EVENT_NAME = "Event name"
        private const val EVENT_VALUE = "Event value"

        @Parameterized.Parameters(name = "{0} -> {1}")
        @JvmStatic
        fun data(): List<Array<Any?>> = listOf(
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_INIT, "Attribution"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_START, "Session start"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_CLIENT, "$EVENT_NAME with value $EVENT_VALUE"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_REFERRER, "Referrer"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_ALIVE, "Session heartbeat"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_FIRST, "The very first event"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_OPEN, "Open"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_UPDATE, "Update"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_PROFILE, "User profile update"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_REVENUE, "Revenue"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_ANR, "ANR"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_CRASH, "Crash: $EVENT_NAME"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_PROTOBUF_ERROR, "Error: $EVENT_NAME"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_ECOMMERCE, "E-Commerce"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_AD_REVENUE, "Ad revenue (ILRD)"),
            arrayOf(EventProto.ReportMessage.Session.Event.EVENT_CLIENT_EXTERNAL_ATTRIBUTION, "External attribution"),
        )
    }

    private val event = EventProto.ReportMessage.Session.Event().apply {
        type = eventType
        name = EVENT_NAME
        value = EVENT_VALUE.toByteArray()
    }

    @Test
    fun constructEventLog() {
        assertThat(PublicLogConstructor.constructEventLogForProtoEvent(event, MESSAGE)).contains(expectedValue)
    }
}
