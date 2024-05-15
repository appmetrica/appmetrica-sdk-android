package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler
import io.appmetrica.analytics.logger.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleServiceEventHandler

private const val TAG_PREFIX = "[ModulesEventHandler-%s]"

class ModulesEventHandler(component: ComponentUnit) : ReportComponentHandler(component) {

    private val apiKey = component.componentId.apiKey ?: "empty"
    private val tag = String.format(TAG_PREFIX, apiKey)

    private val processingChain: List<Pair<ModuleServiceEventHandler, ModuleEventHandlerContextProvider>> =
        GlobalServiceLocator.getInstance().moduleEventHandlersHolder.getHandlers(
            apiKey
        ).map {
            it.value to ModuleEventHandlerContextProvider(component, it.key)
        }

    override fun process(reportData: CounterReport): Boolean {
        DebugLogger.info(
            tag,
            "Apply ${processingChain.size} module handlers to report with type = " +
                "${reportData.type}; customType = ${reportData.customType}; name = ${reportData.name}"
        )
        return processingChain.any { (handler, contextProvider) ->
            handler.handle(contextProvider.getContext(reportData), reportData)
        }
    }
}
