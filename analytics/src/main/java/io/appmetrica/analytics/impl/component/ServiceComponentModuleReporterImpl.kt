package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.coreapi.internal.event.ServiceEvent
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentModuleReporter
import io.appmetrica.analytics.impl.ServiceEventToCoreServiceEventConverter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ServiceComponentModuleReporterImpl(
    private val componentUnit: ComponentUnit,
) : ServiceComponentModuleReporter {

    private val tag = "[ServiceComponentModuleReporterImpl]"

    override fun handleReport(serviceEvent: ServiceEvent) {
        DebugLogger.info(tag, "handleReport: $serviceEvent")
        val coreServiceEvent = ServiceEventToCoreServiceEventConverter.convert(serviceEvent)
        componentUnit.handleReport(coreServiceEvent)
    }
}
