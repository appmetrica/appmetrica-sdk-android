package io.appmetrica.analytics.modulesapi.internal.event

import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi

interface ModuleEventHandler {

    fun handle(context: ModuleEventHandlerContext, reportApi: CounterReportApi): Boolean
}
