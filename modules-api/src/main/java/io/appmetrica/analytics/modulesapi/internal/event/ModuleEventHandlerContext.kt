package io.appmetrica.analytics.modulesapi.internal.event

import io.appmetrica.analytics.modulesapi.internal.ModulePreferences

interface ModuleEventHandlerContext {

    val modulePreferences: ModulePreferences

    val legacyModulePreferences: ModulePreferences

    val eventReporter: ModuleEventHandlerReporter
}
