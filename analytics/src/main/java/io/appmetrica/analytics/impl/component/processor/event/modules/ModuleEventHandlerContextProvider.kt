package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.component.EventSaver
import io.appmetrica.analytics.impl.modules.LegacyModulePreferenceAdapter
import io.appmetrica.analytics.impl.modules.ModulePreferencesAdapter
import io.appmetrica.analytics.modulesapi.internal.event.ModuleEventHandlerContext

class ModuleEventHandlerContextProvider(component: ComponentUnit, moduleIdentifier: String) {

    private val legacyModulePreferenceAdapter = LegacyModulePreferenceAdapter(component.componentPreferences)
    private val modulePreferenceAdapter = ModulePreferencesAdapter(moduleIdentifier, component.componentPreferences)
    private val eventSaver: EventSaver = component.eventSaver

    fun getContext(currentReport: CounterReport): ModuleEventHandlerContext = ModuleEventHandlerContextImpl(
        modulePreferenceAdapter,
        legacyModulePreferenceAdapter,
        ModuleEventReporter(eventSaver, currentReport)
    )
}
