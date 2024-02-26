package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.modulesapi.internal.ModulePreferences
import io.appmetrica.analytics.modulesapi.internal.StorageProvider
import java.io.File

internal class StorageProviderImpl(
    private val context: Context,
    private val preferencesDbStorage: SimplePreferenceStorage,
    override val dbStorage: SQLiteOpenHelper
) : StorageProvider {

    override fun modulePreferences(moduleIdentifier: String): ModulePreferences =
        ModulePreferencesAdapter(moduleIdentifier, preferencesDbStorage)

    override fun legacyModulePreferences(): ModulePreferences =
        LegacyModulePreferenceAdapter(preferencesDbStorage)

    override val appFileStorage: File?
        get() = FileUtils.getAppStorageDirectory(context)

    override val appDataStorage: File?
        get() = FileUtils.getAppDataDir(context)

    override val sdkDataStorage: File?
        get() = FileUtils.sdkStorage(context)
}
