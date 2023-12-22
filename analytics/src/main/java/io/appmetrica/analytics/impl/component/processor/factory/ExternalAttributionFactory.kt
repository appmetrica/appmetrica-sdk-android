package io.appmetrica.analytics.impl.component.processor.factory

import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler

class ExternalAttributionFactory(
    provider: ReportingHandlerProvider
) : JustSaveToDataBaseFactory(provider) {

    override fun addHandlers(reportHandlers: MutableList<ReportComponentHandler>) {
        reportHandlers.add(provider.externalAttributionHandler)
        super.addHandlers(reportHandlers)
    }
}
