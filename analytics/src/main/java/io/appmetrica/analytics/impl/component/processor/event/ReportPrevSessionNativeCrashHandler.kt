package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.ComponentUnit

class ReportPrevSessionNativeCrashHandler(component: ComponentUnit) : ReportComponentHandler(component) {
    override fun process(reportData: CounterReport): Boolean {
        component.eventSaver.saveReportFromPrevSession(reportData)
        return true
    }
}
