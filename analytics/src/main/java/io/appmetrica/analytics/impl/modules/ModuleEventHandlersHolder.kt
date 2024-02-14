package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.logger.internal.YLogger
import io.appmetrica.analytics.modulesapi.internal.event.ModuleEventHandlerFactory

private const val TAG = "[ModuleEventHandlersHolder]"

class ModuleEventHandlersHolder {

    private val _handlers = LinkedHashMap<String, ModuleEventHandlerFactory>()

    fun getHandlers(tag: String) = _handlers.mapValues { it.value.createEventHandler(tag) }

    @Synchronized
    fun register(identifier: String, handler: ModuleEventHandlerFactory) {
        YLogger.info(TAG, "Register new handler factory with identifier = $identifier")
        _handlers[identifier] = handler
    }
}
