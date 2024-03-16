package io.appmetrica.analytics.modulesapi.internal.service.event

import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi

interface ModuleServiceEventHandler {

    fun handle(context: ModuleEventServiceHandlerContext, reportApi: CounterReportApi): Boolean
}
