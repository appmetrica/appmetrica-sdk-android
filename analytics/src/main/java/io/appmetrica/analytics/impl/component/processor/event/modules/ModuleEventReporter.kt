package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.EventSaver
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerReporter

internal class ModuleEventReporter(
    override val apiKey: String?,
    override val isMain: Boolean,
    private val eventSaver: EventSaver,
    private val prototype: CounterReport
) : ModuleEventServiceHandlerReporter {

    private val tag = "[ModuleEventReporter]"

    override fun report(report: CounterReportApi) {
        val newReport = CounterReport.formReportCopyingMetadata(prototype).apply {
            type = report.type
            customType = report.customType
            name = report.name
            report.value?.let { value = it }
            report.valueBytes?.let { valueBytes = it }
            bytesTruncated = report.bytesTruncated
        }
        DebugLogger.info(tag, "new report: $newReport")
        eventSaver.identifyAndSaveReport(newReport)
    }
}
