package io.appmetrica.analytics.impl.db.constants.migrations

import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript
import io.appmetrica.analytics.impl.db.constants.Constants

internal class ClientDatabaseUpgradeScriptToV112 : DatabaseScript() {

    override fun runScript(database: SQLiteDatabase) {
        database.delete(
            Constants.PreferencesTable.TABLE_NAME,
            Constants.PreferencesTable.DELETE_WHERE_KEY,
            arrayOf("NEXT_STARTUP_TIME")
        )
    }
}
