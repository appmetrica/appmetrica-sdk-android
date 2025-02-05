package io.appmetrica.analytics.modulesapi.internal.common

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
}
