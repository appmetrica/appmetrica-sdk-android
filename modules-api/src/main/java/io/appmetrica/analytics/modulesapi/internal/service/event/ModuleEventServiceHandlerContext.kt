package io.appmetrica.analytics.modulesapi.internal.service.event

import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences

/**
 * Context passed to [ModuleServiceEventHandler.handle] when a module processes a service event.
 *
 * Provides access to module preferences and a reporter for sending related events back to analytics.
 */
interface ModuleEventServiceHandlerContext {

    /**
     * Persistent preferences scoped to the current module.
     */
    val modulePreferences: ModulePreferences

    /**
     * Legacy preferences storage for migration from older module data formats.
     */
    val legacyModulePreferences: ModulePreferences

    /**
     * Reporter for sending a new [io.appmetrica.analytics.coreapi.internal.event.ServiceEvent]
     * related to the event currently being handled.
     */
    val eventReporter: ModuleEventServiceHandlerReporter
}
