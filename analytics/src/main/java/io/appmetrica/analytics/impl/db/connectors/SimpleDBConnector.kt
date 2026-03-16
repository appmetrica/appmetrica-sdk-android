package io.appmetrica.analytics.impl.db.connectors

import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.impl.db.DatabaseStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class SimpleDBConnector(private val storage: DatabaseStorage) : DBConnector {

    private val tag = "[SimpleDBConnector]"

    override fun openDb(): SQLiteDatabase? {
        return try {
            storage.writableDatabase
        } catch (ex: Throwable) {
            DebugLogger.warning(tag, "Something went wrong while opening database\n$ex")
            null
        }
    }

    override fun closeDb(db: SQLiteDatabase?) {
        // Do nothing
    }
}
