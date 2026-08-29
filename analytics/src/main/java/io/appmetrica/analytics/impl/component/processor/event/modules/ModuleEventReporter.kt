package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi
import io.appmetrica.analytics.impl.CoreServiceEvent
import io.appmetrica.analytics.impl.component.EventSaver
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerReporter

internal class ModuleEventReporter(
    override val apiKey: String?,
    override val isMain: Boolean,
    private val eventSaver: EventSaver,
    private val prototype: CoreServiceEvent
) : ModuleEventServiceHandlerReporter {

    private val tag = "[ModuleEventReporter]"

    override fun report(report: CounterReportApi) {
        val serviceEvent = CoreServiceEvent.formReportCopyingMetadata(prototype).apply {
            type = report.type
            customType = report.customType
            name = report.name
            report.value?.let { value = it }
            report.valueBytes?.let { valueBytes = it }
            valueProtocolVersion = report.valueProtocolVersion
            bytesTruncated = report.bytesTruncated
        }
        DebugLogger.info(tag, "new report: $serviceEvent")
        eventSaver.identifyAndSaveReport(serviceEvent)
    }
}
