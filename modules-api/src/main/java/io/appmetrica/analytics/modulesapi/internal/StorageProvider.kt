package io.appmetrica.analytics.modulesapi.internal

import android.database.sqlite.SQLiteOpenHelper
import java.io.File

interface StorageProvider {

    fun modulePreferences(moduleIdentifier: String): ModulePreferences

    fun legacyModulePreferences(): ModulePreferences

    val dbStorage: SQLiteOpenHelper

    val appFileStorage: File?

    val appDataStorage: File?

    val sdkDataStorage: File?
}
