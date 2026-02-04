package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.component.EventSaver
import io.appmetrica.analytics.impl.modules.LegacyModulePreferenceAdapter
import io.appmetrica.analytics.impl.modules.ModulePreferencesAdapter
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerContext

internal class ModuleEventHandlerContextProvider(component: ComponentUnit, moduleIdentifier: String) {

    private val legacyModulePreferenceAdapter = LegacyModulePreferenceAdapter(component.componentPreferences)
    private val modulePreferenceAdapter = ModulePreferencesAdapter(moduleIdentifier, component.componentPreferences)
    private val eventSaver: EventSaver = component.eventSaver
    private val isMain: Boolean = component.componentId.isMain

    private val apiKey: String? = component.componentId.apiKey

    fun getContext(
        currentReport: CounterReport
    ): ModuleEventServiceHandlerContext = ModuleEventServiceHandlerContextImpl(
        modulePreferenceAdapter,
        legacyModulePreferenceAdapter,
        ModuleEventReporter(apiKey, isMain, eventSaver, currentReport)
    )
}
