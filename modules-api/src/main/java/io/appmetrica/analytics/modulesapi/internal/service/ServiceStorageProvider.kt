package io.appmetrica.analytics.modulesapi.internal.service

import android.database.sqlite.SQLiteOpenHelper
import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage
import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences
import java.io.File

interface ServiceStorageProvider {

    fun modulePreferences(moduleIdentifier: String): ModulePreferences

    fun legacyModulePreferences(): ModulePreferences

    val dbStorage: SQLiteOpenHelper

    val tempCacheStorage: TempCacheStorage

    val appFileStorage: File?

    val appDataStorage: File?

    val sdkDataStorage: File?
}
