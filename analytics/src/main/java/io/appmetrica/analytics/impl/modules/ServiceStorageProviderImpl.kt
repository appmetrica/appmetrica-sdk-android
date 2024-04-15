package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory
import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences
import io.appmetrica.analytics.modulesapi.internal.service.ServiceStorageProvider
import java.io.File

internal class ServiceStorageProviderImpl(
    private val context: Context,
    private val preferencesDbStorage: SimplePreferenceStorage,
    override val dbStorage: SQLiteOpenHelper
) : ServiceStorageProvider {

    override fun modulePreferences(moduleIdentifier: String): ModulePreferences =
        ModulePreferencesAdapter(moduleIdentifier, preferencesDbStorage)

    override fun legacyModulePreferences(): ModulePreferences =
        LegacyModulePreferenceAdapter(preferencesDbStorage)

    override val tempCacheStorage: TempCacheStorage
        get() = DatabaseStorageFactory.getInstance(context).tempCacheStorageForService

    override val appFileStorage: File?
        get() = FileUtils.getAppStorageDirectory(context)

    override val appDataStorage: File?
        get() = FileUtils.getAppDataDir(context)

    override val sdkDataStorage: File?
        get() = FileUtils.sdkStorage(context)
}
