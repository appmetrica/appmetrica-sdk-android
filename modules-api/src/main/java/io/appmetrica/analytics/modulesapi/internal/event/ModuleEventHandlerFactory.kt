package io.appmetrica.analytics.modulesapi.internal.event

fun interface ModuleEventHandlerFactory {

    fun createEventHandler(tag: String): ModuleEventHandler
}
