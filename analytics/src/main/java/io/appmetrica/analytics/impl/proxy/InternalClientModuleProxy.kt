package io.appmetrica.analytics.impl.proxy

import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.ModulesFacade
import io.appmetrica.analytics.modulesapi.internal.common.InternalClientModuleFacade
import io.appmetrica.analytics.modulesapi.internal.common.InternalModuleEvent

class InternalClientModuleProxy : InternalClientModuleFacade {

    override fun reportEvent(internalModuleEvent: InternalModuleEvent) {
        val moduleEvent = ModuleEvent.newBuilder(internalModuleEvent.type)
            .withName(internalModuleEvent.name)
            .withValue(internalModuleEvent.value)
            .apply {
                internalModuleEvent.serviceDataReporterType?.let {
                    withServiceDataReporterType(it)
                }
            }
            .withExtras(internalModuleEvent.getExtras())
            .withAttributes(internalModuleEvent.getAttributes())
            .withEnvironment(internalModuleEvent.getEnvironment())
            .build()
        ModulesFacade.reportEvent(moduleEvent)
    }
}
