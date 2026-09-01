package io.appmetrica.analytics.modulesapi.internal.service.event

import io.appmetrica.analytics.coreapi.internal.event.ServiceEvent

interface ModuleServiceEventHandler {

    fun handle(context: ModuleEventServiceHandlerContext, serviceEvent: ServiceEvent): Boolean
}
