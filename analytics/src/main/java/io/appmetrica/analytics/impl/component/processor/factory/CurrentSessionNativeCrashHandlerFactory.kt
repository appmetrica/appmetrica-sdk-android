package io.appmetrica.analytics.impl.component.processor.factory

import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler

class CurrentSessionNativeCrashHandlerFactory(
    provider: ReportingHandlerProvider
) : HandlersFactory<ReportComponentHandler>(provider) {

    override fun addHandlers(reportHandlers: MutableList<ReportComponentHandler>) {
        reportHandlers.add(provider.reportSaveToDatabaseHandler)
        reportHandlers.add(provider.reportPurgeBufferHandler)
        reportHandlers.add(provider.reportSessionStopHandler)
    }
}
