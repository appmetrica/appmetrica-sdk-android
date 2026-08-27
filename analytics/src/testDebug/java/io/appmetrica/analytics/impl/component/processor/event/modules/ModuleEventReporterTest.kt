package io.appmetrica.analytics.impl.component.processor.event.modules

import android.annotation.SuppressLint
import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi
import io.appmetrica.analytics.impl.ServiceEvent
import io.appmetrica.analytics.impl.component.EventSaver
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@SuppressLint("RobolectricUsage")
@RunWith(RobolectricTestRunner::class)
internal class ModuleEventReporterTest : CommonTest() {

    private val eventSaver = mock<EventSaver>()

    private val prototypeServiceEvent = ServiceEvent()

    private val apiKey = UUID.randomUUID().toString()
    private val typeValue = 22
    private val customTypeValue = 44
    private val nameValue = "Some event name"
    private val reportValue = "Some event value"
    private val valueBytesValue = ByteArray(128) { int -> int.toByte() }
    private val valueProtocolVersionValue = 3
    private val bytesTruncatedValue = 123

    private val moduleReport = mock<CounterReportApi> {
        on { type } doReturn typeValue
        on { customType } doReturn customTypeValue
        on { name } doReturn nameValue
        on { value } doReturn reportValue
        on { valueBytes } doReturn valueBytesValue
        on { valueProtocolVersion } doReturn valueProtocolVersionValue
        on { bytesTruncated } doReturn bytesTruncatedValue
    }

    private val isMain = true

    private lateinit var moduleEventReporter: ModuleEventReporter

    @Before
    fun setUp() {
        moduleEventReporter = ModuleEventReporter(apiKey, isMain, eventSaver, prototypeServiceEvent)
    }

    @Test
    fun report() {
        moduleEventReporter.report(moduleReport)
        val captor = argumentCaptor<ServiceEvent>()
        verify(eventSaver).identifyAndSaveReport(captor.capture())
        val saved = captor.firstValue
        assertThat(saved.type).isEqualTo(typeValue)
        assertThat(saved.customType).isEqualTo(customTypeValue)
        assertThat(saved.name).isEqualTo(nameValue)
        assertThat(saved.valueBytes).isEqualTo(valueBytesValue)
        assertThat(saved.valueProtocolVersion).isEqualTo(valueProtocolVersionValue)
        assertThat(saved.bytesTruncated).isEqualTo(bytesTruncatedValue)
    }

    @Test
    fun `report without string value`() {
        whenever(moduleReport.value).thenReturn(null)
        moduleEventReporter.report(moduleReport)
        val captor = argumentCaptor<ServiceEvent>()
        verify(eventSaver).identifyAndSaveReport(captor.capture())
        val saved = captor.firstValue
        assertThat(saved.type).isEqualTo(typeValue)
        assertThat(saved.customType).isEqualTo(customTypeValue)
        assertThat(saved.name).isEqualTo(nameValue)
        assertThat(saved.valueBytes).isEqualTo(valueBytesValue)
        assertThat(saved.valueProtocolVersion).isEqualTo(valueProtocolVersionValue)
        assertThat(saved.bytesTruncated).isEqualTo(bytesTruncatedValue)
    }

    @Test
    fun `report without bytes value`() {
        whenever(moduleReport.valueBytes).thenReturn(null)
        moduleEventReporter.report(moduleReport)
        val captor = argumentCaptor<ServiceEvent>()
        verify(eventSaver).identifyAndSaveReport(captor.capture())
        val saved = captor.firstValue
        assertThat(saved.type).isEqualTo(typeValue)
        assertThat(saved.customType).isEqualTo(customTypeValue)
        assertThat(saved.name).isEqualTo(nameValue)
        assertThat(saved.value).isEqualTo(reportValue)
        assertThat(saved.valueProtocolVersion).isEqualTo(valueProtocolVersionValue)
        assertThat(saved.bytesTruncated).isEqualTo(bytesTruncatedValue)
    }

    @Test
    fun `main for true`() {
        assertThat(ModuleEventReporter(apiKey, isMain, eventSaver, prototypeServiceEvent).isMain).isTrue()
    }

    @Test
    fun `main for false`() {
        assertThat(ModuleEventReporter(apiKey, false, eventSaver, prototypeServiceEvent).isMain).isFalse()
    }

    @Test
    fun `api key`() {
        assertThat(ModuleEventReporter(apiKey, isMain, eventSaver, prototypeServiceEvent).apiKey).isEqualTo(apiKey)
    }

    @Test
    fun `api key for null`() {
        assertThat(ModuleEventReporter(null, isMain, eventSaver, prototypeServiceEvent).apiKey).isNull()
    }
}
