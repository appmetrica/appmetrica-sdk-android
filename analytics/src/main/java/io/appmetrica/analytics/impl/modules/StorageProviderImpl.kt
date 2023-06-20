package io.appmetrica.analytics.impl.modules

import android.database.sqlite.SQLiteOpenHelper
import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.modulesapi.internal.ModulePreferences
import io.appmetrica.analytics.modulesapi.internal.StorageProvider

internal class StorageProviderImpl(
    private val preferencesDbStorage: SimplePreferenceStorage,
    override val dbStorage: SQLiteOpenHelper
) : StorageProvider {

    override fun modulePreferences(moduleIdentifier: String): ModulePreferences =
        ModulePreferencesAdapter(moduleIdentifier, preferencesDbStorage)

    override fun legacyModulePreferences(): ModulePreferences =
        LegacyModulePreferenceAdapter(preferencesDbStorage)
}
