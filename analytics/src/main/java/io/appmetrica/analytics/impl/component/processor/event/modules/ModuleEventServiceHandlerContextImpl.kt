package io.appmetrica.analytics.impl.component.processor.event.modules

import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerContext
import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerReporter

internal class ModuleEventServiceHandlerContextImpl(
    override val modulePreferences: ModulePreferences,
    override val legacyModulePreferences: ModulePreferences,
    override val eventReporter: ModuleEventServiceHandlerReporter
) : ModuleEventServiceHandlerContext
