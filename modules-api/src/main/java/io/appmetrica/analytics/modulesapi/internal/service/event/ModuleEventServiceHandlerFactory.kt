package io.appmetrica.analytics.modulesapi.internal.service.event

fun interface ModuleEventServiceHandlerFactory {

    fun createEventHandler(tag: String): ModuleServiceEventHandler
}
