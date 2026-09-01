package io.appmetrica.analytics.modulesapi.internal.service.event

import io.appmetrica.analytics.coreapi.internal.event.ServiceEvent

/**
 * Reporter that allows a module event handler to send a new [ServiceEvent] back to analytics.
 *
 * Available via [ModuleEventServiceHandlerContext.eventReporter] while handling an incoming event.
 * Reported events inherit metadata from the event currently being processed.
 */
interface ModuleEventServiceHandlerReporter {

    /**
     * API key of the reporter component this handler is bound to.
     * May be `null` if the component has no API key.
     */
    val apiKey: String?

    /**
     * `true` if this reporter belongs to the main reporter component, `false` otherwise.
     */
    val isMain: Boolean

    /**
     * Reports [serviceEvent] to analytics.
     *
     * Metadata (session, identifiers, etc.) is copied from the event currently being handled.
     */
    fun report(serviceEvent: ServiceEvent)
}
