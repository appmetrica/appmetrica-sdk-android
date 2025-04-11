package io.appmetrica.analytics.impl.component.processor.factory

import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler

class PrevSessionNativeCrashHandlerFactory(
    provider: ReportingHandlerProvider
) : HandlersFactory<ReportComponentHandler>(provider) {

    override fun addHandlers(reportHandlers: MutableList<ReportComponentHandler>) {
        reportHandlers.add(provider.reportPrevSessionEventHandler)
        reportHandlers.add(provider.reportPurgeBufferHandler)
    }
}
