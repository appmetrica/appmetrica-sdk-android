package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.modulesapi.internal.ModulePreferences
import io.appmetrica.analytics.modulesapi.internal.event.ModuleEventHandlerContext
import io.appmetrica.analytics.modulesapi.internal.event.ModuleEventHandlerReporter

class ModuleEventHandlerContextImpl(
    override val modulePreferences: ModulePreferences,
    override val legacyModulePreferences: ModulePreferences,
    override val eventReporter: ModuleEventHandlerReporter
) : ModuleEventHandlerContext
