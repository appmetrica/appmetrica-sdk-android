package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.component.ComponentUnit

class SaveSessionExtrasHandler(component: ComponentUnit) : ReportComponentHandler(component) {

    private val tag =
        "[SaveSessionExtrasHandler-${component.componentId.apiKey?.let { Utils.createPartialApiKey(it) }}]"

    override fun process(reportData: CounterReport): Boolean {
        reportData.extras.forEach {
            YLogger.info(tag, "Update session extra with key = `${it.key}`; value size = ${it.value.size}")
            component.sessionExtrasHolder.put(it.key, it.value)
        }
        return true
    }
}
