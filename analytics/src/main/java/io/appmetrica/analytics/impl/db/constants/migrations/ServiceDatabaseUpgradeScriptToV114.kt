package io.appmetrica.analytics.impl.db.constants.migrations

import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript
import io.appmetrica.analytics.impl.db.constants.TempCacheTable

internal class ServiceDatabaseUpgradeScriptToV114 : DatabaseScript() {

    override fun runScript(database: SQLiteDatabase) {
        database.execSQL(TempCacheTable.CREATE_TABLE)
    }
}
