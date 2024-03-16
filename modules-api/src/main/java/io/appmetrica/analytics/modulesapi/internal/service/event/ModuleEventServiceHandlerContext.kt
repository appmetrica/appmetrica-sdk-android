package io.appmetrica.analytics.modulesapi.internal.service.event

import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences

interface ModuleEventServiceHandlerContext {

    val modulePreferences: ModulePreferences

    val legacyModulePreferences: ModulePreferences

    val eventReporter: ModuleEventServiceHandlerReporter
}
