package io.appmetrica.analytics.impl.component.processor.factory

import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler

class PrevSessionUnhandledExceptionFromFileFactory(
    provider: ReportingHandlerProvider
) : HandlersFactory<ReportComponentHandler>(provider) {

    override fun addHandlers(reportHandlers: MutableList<ReportComponentHandler>) {
        reportHandlers.add(provider.reportCrashMetaInformation)
        reportHandlers.add(provider.reportPrevSessionEventHandler)
    }
}
