package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class SaveSessionExtrasHandler(component: ComponentUnit) : ReportComponentHandler(component) {

    private val tag =
        "[SaveSessionExtrasHandler-${component.componentId.anonymizedApiKey}]"

    override fun process(reportData: CounterReport): Boolean {
        reportData.extras.forEach {
            DebugLogger.info(
                tag,
                "Update session extra with key = `${it.key}`; value size = ${it.value.size}"
            )
            component.sessionExtrasHolder.put(it.key, it.value)
        }
        return true
    }
}
