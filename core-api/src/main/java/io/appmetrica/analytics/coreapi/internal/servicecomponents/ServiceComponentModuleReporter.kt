package io.appmetrica.analytics.coreapi.internal.servicecomponents

import io.appmetrica.analytics.coreapi.internal.event.ServiceEvent

/**
 * Reporter that allows a service-side module to send a [ServiceEvent] to analytics.
 *
 * Obtained from [ServiceModuleReporterComponentContext.reporter] after the main reporter
 * component is created (see [ServiceModuleReporterComponentLifecycleListener]).
 * Use this for proactive reporting outside of event handling.
 */
interface ServiceComponentModuleReporter {

    /**
     * Reports [serviceEvent] to the corresponding analytics component.
     */
    fun handleReport(serviceEvent: ServiceEvent)
}
