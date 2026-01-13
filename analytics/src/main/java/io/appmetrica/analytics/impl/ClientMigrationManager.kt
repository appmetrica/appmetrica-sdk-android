package io.appmetrica.analytics.impl

import android.util.SparseArray
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage

class ClientMigrationManager(
    private val clientStorage: PreferencesClientDbStorage
) : MigrationManager() {

    override fun getScripts(): SparseArray<MigrationScript> {
        return SparseArray()
    }

    override fun getLastApiLevel(): Int {
        return clientStorage.getClientApiLevel(-1).toInt()
    }

    override fun putLastApiLevel(apiLevel: Int) {
        clientStorage.putClientApiLevel(apiLevel.toLong())
    }
}
