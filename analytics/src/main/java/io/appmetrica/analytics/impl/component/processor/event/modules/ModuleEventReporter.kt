package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.EventSaver
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerReporter

class ModuleEventReporter(
    private val eventSaver: EventSaver,
    private val prototype: CounterReport
) : ModuleEventServiceHandlerReporter {

    override fun report(report: CounterReportApi) {
        val newReport = CounterReport.formReportCopyingMetadata(prototype).apply {
            type = report.type
            customType = report.customType
            name = report.name
            value = report.value
            valueBytes = report.valueBytes
            bytesTruncated = report.bytesTruncated
        }
        eventSaver.identifyAndSaveReport(newReport)
    }
}
