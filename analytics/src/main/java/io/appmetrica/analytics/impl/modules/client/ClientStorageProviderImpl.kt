package io.appmetrica.analytics.impl.modules.client

import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.impl.modules.ModulePreferencesAdapter
import io.appmetrica.analytics.modulesapi.internal.client.ClientStorageProvider
import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences

internal class ClientStorageProviderImpl(
    private val preferencesDbStorage: SimplePreferenceStorage
) : ClientStorageProvider {

    override fun modulePreferences(moduleIdentifier: String): ModulePreferences =
        ModulePreferencesAdapter(moduleIdentifier, preferencesDbStorage)
}
