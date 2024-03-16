package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.logger.internal.YLogger
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerFactory

private const val TAG = "[ModuleEventHandlersHolder]"

class ModuleEventHandlersHolder {

    private val _handlers = LinkedHashMap<String, ModuleEventServiceHandlerFactory>()

    fun getHandlers(tag: String) = _handlers.mapValues { it.value.createEventHandler(tag) }

    @Synchronized
    fun register(identifier: String, handler: ModuleEventServiceHandlerFactory) {
        YLogger.info(TAG, "Register new handler factory with identifier = $identifier")
        _handlers[identifier] = handler
    }
}
