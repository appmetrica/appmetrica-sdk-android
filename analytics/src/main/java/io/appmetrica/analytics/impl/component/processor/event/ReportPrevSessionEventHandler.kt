package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ReportPrevSessionEventHandler(
    component: ComponentUnit
) : ReportComponentHandler(component) {

    private val tag = "[ReportPrevSessionEventHandler]"

    override fun process(reportData: CounterReport): Boolean {
        DebugLogger.info(tag, "handle report: ${reportData.name}")

        // If saving failed for some reason, something went wrong and further handlers should not be applied.
        return !component.eventSaver.saveReportFromPrevSession(reportData)
    }
}
