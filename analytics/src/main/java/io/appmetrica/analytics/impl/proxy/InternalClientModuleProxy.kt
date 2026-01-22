package io.appmetrica.analytics.impl.proxy

import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.ModulesFacade
import io.appmetrica.analytics.impl.adrevenue.ModuleAdRevenueConverter
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.common.InternalClientModuleFacade
import io.appmetrica.analytics.modulesapi.internal.common.InternalModuleEvent

internal class InternalClientModuleProxy : InternalClientModuleFacade {

    private val converter = ModuleAdRevenueConverter()

    override fun reportEvent(internalModuleEvent: InternalModuleEvent) {
        val moduleEvent = ModuleEvent.newBuilder(internalModuleEvent.type)
            .withName(internalModuleEvent.name)
            .withValue(internalModuleEvent.value)
            .apply {
                internalModuleEvent.serviceDataReporterType?.let {
                    withServiceDataReporterType(it)
                }
            }
            .apply {
                internalModuleEvent.category?.toModuleEventCategory()?.let {
                    withCategory(it)
                }
            }
            .withExtras(internalModuleEvent.getExtras())
            .withAttributes(internalModuleEvent.getAttributes())
            .withEnvironment(internalModuleEvent.getEnvironment())
            .build()
        ModulesFacade.reportEvent(moduleEvent)
    }

    override fun reportAdRevenue(moduleAdRevenue: ModuleAdRevenue) {
        ModulesFacade.reportAdRevenue(converter.convert(moduleAdRevenue), moduleAdRevenue.autoCollected)
    }

    private fun InternalModuleEvent.Category?.toModuleEventCategory(): ModuleEvent.Category? = when (this) {
        InternalModuleEvent.Category.SYSTEM -> ModuleEvent.Category.SYSTEM
        InternalModuleEvent.Category.GENERAL -> ModuleEvent.Category.GENERAL
        else -> null
    }
}
