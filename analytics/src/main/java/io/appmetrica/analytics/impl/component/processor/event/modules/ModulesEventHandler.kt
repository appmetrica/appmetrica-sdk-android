package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.ServiceEvent
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleServiceEventHandler

private const val TAG_PREFIX = "[ModulesEventHandler-%s]"

internal class ModulesEventHandler(component: ComponentUnit) : ReportComponentHandler(component) {

    private val apiKey = component.componentId.apiKey ?: "empty"
    private val tag = String.format(TAG_PREFIX, apiKey)

    private val processingChain: List<Pair<ModuleServiceEventHandler, ModuleEventHandlerContextProvider>> =
        GlobalServiceLocator.getInstance().moduleEventHandlersHolder.getHandlers(
            apiKey
        ).map {
            it.value to ModuleEventHandlerContextProvider(component, it.key)
        }

    override fun process(serviceEvent: ServiceEvent): Boolean {
        if (component.vitalComponentDataProvider.isFirstEventDone) {
            DebugLogger.info(
                tag,
                "Apply ${processingChain.size} module handlers to report with type = " +
                    "${serviceEvent.type}; customType = ${serviceEvent.customType}; name = ${serviceEvent.name}"
            )
            return processingChain.any { (handler, contextProvider) ->
                handler.handle(contextProvider.getContext(serviceEvent), serviceEvent)
            }
        } else {
            DebugLogger.info(tag, "First event hasn't happened yet. Ignore.")
            return false
        }
    }
}
