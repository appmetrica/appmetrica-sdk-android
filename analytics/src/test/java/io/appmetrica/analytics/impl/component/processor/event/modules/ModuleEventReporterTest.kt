package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.EventSaver
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ModuleEventReporterTest : CommonTest() {

    private val eventSaver = mock<EventSaver>()

    private val prototypeReport = mock<CounterReport>()

    private val typeValue = 22
    private val customTypeValue = 44
    private val nameValue = "Some event name"
    private val reportValue = "Some event value"
    private val valueBytesValue = ByteArray(128) { int -> int.toByte() }
    private val bytesTruncatedValue = 123
    private val moduleReport = mock<CounterReportApi> {
        on { type } doReturn typeValue
        on { customType } doReturn customTypeValue
        on { name } doReturn nameValue
        on { value } doReturn reportValue
        on { valueBytes } doReturn valueBytesValue
        on { bytesTruncated } doReturn bytesTruncatedValue
    }

    private val newReport = mock<CounterReport>()

    @get:Rule
    val counterReportMockedStaticRule = MockedStaticRule(CounterReport::class.java)

    private lateinit var moduleEventReporter: ModuleEventReporter

    @Before
    fun setUp() {
        whenever(CounterReport.formReportCopyingMetadata(prototypeReport)).thenReturn(newReport)
        moduleEventReporter = ModuleEventReporter(eventSaver, prototypeReport)
    }

    @Test
    fun report() {
        moduleEventReporter.report(moduleReport)
        inOrder(newReport, eventSaver) {
            verify(newReport).type = typeValue
            verify(newReport).customType = customTypeValue
            verify(newReport).name = nameValue
            verify(newReport).value = reportValue
            verify(newReport).valueBytes = valueBytesValue
            verify(newReport).bytesTruncated = bytesTruncatedValue
            verify(eventSaver).identifyAndSaveReport(newReport)
        }
    }
}
