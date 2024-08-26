package io.appmetrica.analytics.modulesapi.internal.service.event

abstract class ModuleEventServiceHandlerFactory {

    abstract fun createEventHandler(tag: String): ModuleServiceEventHandler
}
