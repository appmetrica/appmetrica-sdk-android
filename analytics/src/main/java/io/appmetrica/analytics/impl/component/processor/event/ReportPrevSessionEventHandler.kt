package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class ReportPrevSessionEventHandler(
    component: ComponentUnit
) : ReportComponentHandler(component) {

    private val tag = "[ReportPrevSessionEventHandler]"

    override fun process(reportData: CounterReport): Boolean {
        DebugLogger.info(tag, "handle report: ${reportData.name}")
        component.eventSaver.saveReportFromPrevSession(reportData)
        return false
    }
}
