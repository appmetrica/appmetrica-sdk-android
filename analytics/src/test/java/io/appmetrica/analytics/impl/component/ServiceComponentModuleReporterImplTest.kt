package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleCounterReport
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.ServiceModuleCounterReportToCounterReportConverter
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class ServiceComponentModuleReporterImplTest : CommonTest() {

    private val report: ServiceModuleCounterReport = mock()
    private val counterReport: CounterReport = mock()
    private val componentUnit: ComponentUnit = mock()
    private val converter: ServiceModuleCounterReportToCounterReportConverter = mock {
        on { convert(report) } doReturn counterReport
    }

    private val reporter = ServiceComponentModuleReporterImpl(
        componentUnit,
        converter
    )

    @Test
    fun handleReport() {
        reporter.handleReport(report)

        verify(componentUnit).handleReport(counterReport)
    }
}
