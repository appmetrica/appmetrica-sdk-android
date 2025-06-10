package io.appmetrica.analytics.modulesapi.internal.service.event

import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi

interface ModuleEventServiceHandlerReporter {

    val isMain: Boolean

    fun report(report: CounterReportApi)
}
