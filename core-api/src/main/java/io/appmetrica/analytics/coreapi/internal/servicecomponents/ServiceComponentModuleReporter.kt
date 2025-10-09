package io.appmetrica.analytics.coreapi.internal.servicecomponents

interface ServiceComponentModuleReporter {

    fun handleReport(report: ServiceModuleCounterReport)
}
