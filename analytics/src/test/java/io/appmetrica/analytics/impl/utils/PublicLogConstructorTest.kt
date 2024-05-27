package io.appmetrica.analytics.impl.utils

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PublicLogConstructorTest : CommonTest() {

    @get:Rule
    val eventsManagerRule = MockedStaticRule(EventsManager::class.java)

    @Test
    fun constructCounterReportLog() {
        val type = 42
        whenever(EventsManager.isPublicForLogs(type)).thenReturn(true)
        whenever(EventsManager.shouldLogValue(type)).thenReturn(true)

        val name = "some_name"
        val value = "some_value"

        val reportData = mock<CounterReport> {
            whenever(it.type).thenReturn(type)
            whenever(it.name).thenReturn(name)
            whenever(it.value).thenReturn(value)
        }

        val message = "some message"

        val log = PublicLogConstructor.constructCounterReportLog(reportData, message)

        assertThat(log).isEqualTo("$message: $name with value $value")
    }

    @Test
    fun constructCounterReportLogIfShouldNotLogValue() {
        val type = 42
        whenever(EventsManager.isPublicForLogs(type)).thenReturn(true)
        whenever(EventsManager.shouldLogValue(type)).thenReturn(false)

        val name = "some_name"
        val value = "some_value"

        val reportData = mock<CounterReport> {
            whenever(it.type).thenReturn(type)
            whenever(it.name).thenReturn(name)
            whenever(it.value).thenReturn(value)
        }

        val message = "some message"

        val log = PublicLogConstructor.constructCounterReportLog(reportData, message)

        assertThat(log).isEqualTo("$message: $name")
    }

    @Test
    fun constructCounterReportLogIfIsNotPublicForLogs() {
        val type = 42
        whenever(EventsManager.isPublicForLogs(type)).thenReturn(false)
        whenever(EventsManager.shouldLogValue(type)).thenReturn(true)

        val value = "some_value"

        val reportData = mock<CounterReport> {
            whenever(it.type).thenReturn(type)
            whenever(it.value).thenReturn(value)
        }

        val message = "some message"

        val log = PublicLogConstructor.constructCounterReportLog(reportData, message)

        assertThat(log).isNull()
    }

    @Test
    fun constructEventLog() {
        val message = "some message"
        val event = EventProto.ReportMessage.Session.Event()
        event.type = EventProto.ReportMessage.Session.Event.EVENT_CLIENT
        event.name = "first name"
        event.value = "first value".toByteArray()

        whenever(EventsManager.isSuitableForLogs(event)).thenReturn(true)

        val log = PublicLogConstructor.constructEventLog(event, message)

        assertThat(log).isEqualTo("$message: ${event.name} with value ${String(event.value)}")
    }

    @Test
    fun constructEventLogIfNotSuitableForLogs() {
        val message = "some message"
        val event = EventProto.ReportMessage.Session.Event()
        event.type = EventProto.ReportMessage.Session.Event.EVENT_CLIENT
        event.name = "first name"
        event.value = "first value".toByteArray()

        whenever(EventsManager.isSuitableForLogs(event)).thenReturn(false)

        val log = PublicLogConstructor.constructEventLog(event, message)

        assertThat(log).isNull()
    }
}
