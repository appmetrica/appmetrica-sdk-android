package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentModuleReporter
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleCounterReport
import io.appmetrica.analytics.impl.ServiceModuleCounterReportToCounterReportConverter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ServiceComponentModuleReporterImpl(
    private val componentUnit: ComponentUnit,
    private val converter: ServiceModuleCounterReportToCounterReportConverter =
        ServiceModuleCounterReportToCounterReportConverter()
) : ServiceComponentModuleReporter {

    private val tag = "[ServiceComponentModuleReporterImpl]"

    override fun handleReport(report: ServiceModuleCounterReport) {
        DebugLogger.info(tag, "handleReport: $report")
        val counterReport = converter.convert(report)
        componentUnit.handleReport(counterReport)
    }
}
