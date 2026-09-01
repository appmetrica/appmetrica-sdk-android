package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.coreapi.internal.event.ServiceEvent
import io.appmetrica.analytics.impl.CoreServiceEvent
import io.appmetrica.analytics.impl.ServiceEventToCoreServiceEventConverter
import io.appmetrica.analytics.impl.component.EventSaver
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerReporter

internal class ModuleEventReporter(
    override val apiKey: String?,
    override val isMain: Boolean,
    private val eventSaver: EventSaver,
    private val prototype: CoreServiceEvent,
) : ModuleEventServiceHandlerReporter {

    private val tag = "[ModuleEventReporter]"

    override fun report(serviceEvent: ServiceEvent) {
        val coreServiceEvent = ServiceEventToCoreServiceEventConverter.convert(serviceEvent, prototype)
        DebugLogger.info(tag, "new report: $coreServiceEvent")
        eventSaver.identifyAndSaveReport(coreServiceEvent)
    }
}
