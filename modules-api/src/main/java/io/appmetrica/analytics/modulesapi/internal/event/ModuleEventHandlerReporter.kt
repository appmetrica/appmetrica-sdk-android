package io.appmetrica.analytics.modulesapi.internal.event

import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi

interface ModuleEventHandlerReporter {

    fun report(report: CounterReportApi)
}
