package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.impl.CoreServiceEvent
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ReportPrevSessionEventHandler(
    component: ComponentUnit
) : ReportComponentHandler(component) {

    private val tag = "[ReportPrevSessionEventHandler]"

    override fun process(serviceEvent: CoreServiceEvent): Boolean {
        DebugLogger.info(tag, "handle report: ${serviceEvent.name}")

        // If saving failed for some reason, something went wrong and further handlers should not be applied.
        return !component.eventSaver.saveReportFromPrevSession(serviceEvent)
    }
}
