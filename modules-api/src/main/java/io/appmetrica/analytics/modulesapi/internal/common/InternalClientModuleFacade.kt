package io.appmetrica.analytics.modulesapi.internal.common

import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue

/**
 * Class with methods for communication of different AppMetrica modules.
 */
interface InternalClientModuleFacade {

    /**
     * Sends report with custom parameters.
     *
     * @param internalModuleEvent Event parameters
     */
    fun reportEvent(internalModuleEvent: InternalModuleEvent)

    /**
     * Reports moduleAdRevenue to AppMetrica
     *
     * @param moduleAdRevenue ModuleAdRevenue
     */
    fun reportAdRevenue(moduleAdRevenue: ModuleAdRevenue)
}
