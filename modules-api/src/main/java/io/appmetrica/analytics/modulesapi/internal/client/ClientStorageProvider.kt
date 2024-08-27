package io.appmetrica.analytics.modulesapi.internal.client

import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences

interface ClientStorageProvider {

    fun modulePreferences(moduleIdentifier: String): ModulePreferences
}
