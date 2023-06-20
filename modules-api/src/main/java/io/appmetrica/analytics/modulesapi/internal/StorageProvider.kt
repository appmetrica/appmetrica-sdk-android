package io.appmetrica.analytics.modulesapi.internal

import android.database.sqlite.SQLiteOpenHelper

interface StorageProvider {

    fun modulePreferences(moduleIdentifier: String): ModulePreferences

    fun legacyModulePreferences(): ModulePreferences

    val dbStorage: SQLiteOpenHelper
}
