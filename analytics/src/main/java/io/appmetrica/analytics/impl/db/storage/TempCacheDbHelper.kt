package io.appmetrica.analytics.impl.db.storage

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.db.connectors.DBConnector
import io.appmetrica.analytics.impl.db.constants.TempCacheTable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class TempCacheDbHelper(
    private val dbConnector: DBConnector,
    private val tableName: String
) : TempCacheStorage {

    private val tag = "[TempCacheDbHelper]"

    private val timeProvider = SystemTimeProvider()

    override fun put(scope: String, timestamp: Long, data: ByteArray): Long {
        var database: SQLiteDatabase? = null
        try {
            database = dbConnector.openDb()
            database?.let { db ->
                val values = ContentValues().apply {
                    put(TempCacheTable.Column.SCOPE, scope)
                    put(TempCacheTable.Column.TIMESTAMP, timestamp)
                    put(TempCacheTable.Column.DATA, data)
                }
                val id = db.insertOrThrow(tableName, null, values)
                DebugLogger.info(
                    tag,
                    "Inserted record with scope = $scope; timestamp = $timestamp; data = array[${data.size}]. " +
                        "Id = $id"
                )
                return id
            }
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
        } finally {
            dbConnector.closeDb(database)
        }
        return -1
    }

    override fun get(scope: String): TempCacheStorage.Entry? = get(scope, 1).firstOrNull()

    override fun get(scope: String, limit: Int): List<TempCacheStorage.Entry> {
        var database: SQLiteDatabase? = null
        var dataCursor: Cursor? = null
        val result = mutableListOf<TempCacheStorage.Entry>()
        try {
            database = dbConnector.openDb()
            database?.let { db ->
                dataCursor = db.query(
                    false, // distinct
                    tableName,
                    null, // columns: null means all
                    "${TempCacheTable.Column.SCOPE}=?", // selection
                    arrayOf(scope), // selection args
                    null, // group by
                    null, // having
                    TempCacheTable.Column.ID, // order by
                    "$limit"
                )
                dataCursor?.let { cursor ->
                    while (cursor.moveToNext()) {
                        cursor.toEntry()?.let { entry -> result.add(entry) }
                    }
                }
            }
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
        } finally {
            Utils.closeCursor(dataCursor)
            dbConnector.closeDb(database)
        }
        DebugLogger.info(
            tag,
            "Requested data: scope = $scope; limit = $limit; returned ${result.size} records"
        )
        return result
    }

    override fun remove(id: Long) {
        removeByCondition("${TempCacheTable.Column.ID}=?", arrayOf("$id"))
    }

    override fun removeOlderThan(scope: String, interval: Long) {
        removeByCondition(
            "${TempCacheTable.Column.SCOPE}=? AND ${TempCacheTable.Column.TIMESTAMP}<?",
            arrayOf(scope, "${timeProvider.currentTimeMillis() - interval}")
        )
    }

    private fun removeByCondition(whereClause: String, whereArgs: Array<String>) {
        var database: SQLiteDatabase? = null
        try {
            database = dbConnector.openDb()
            val count = database?.delete(tableName, whereClause, whereArgs)
            DebugLogger.info(tag, "Removed $count records. Where clause = $whereClause; args = $whereArgs")
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
        } finally {
            dbConnector.closeDb(database)
        }
    }

    private fun Cursor.toEntry(): TempCacheStorage.Entry? =
        try {
            TempCacheEntry(
                id = getLong(getColumnIndexOrThrow(TempCacheTable.Column.ID)),
                scope = getString(getColumnIndexOrThrow(TempCacheTable.Column.SCOPE)),
                timestamp = getLong(getColumnIndexOrThrow(TempCacheTable.Column.TIMESTAMP)),
                data = getBlob(getColumnIndexOrThrow(TempCacheTable.Column.DATA))
            )
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
            null
        }
}
