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

internal class TempCacheDbHelper(
    private val dbConnector: DBConnector,
    private val tableName: String
) : TempCacheStorage {

    private val tag = "[TempCacheDbHelper]"

    private val timeProvider = SystemTimeProvider()

    private val bufferedWriter = BufferedTempCacheWriter(1000, ::insertRecords)

    override fun put(scope: String, timestamp: Long, data: ByteArray) {
        bufferedWriter.put(scope, timestamp, data)
    }

    internal fun putDirect(scope: String, timestamp: Long, data: ByteArray): Long {
        return insertRecords(listOf(TempCachePutTask(scope, timestamp, data)))
    }

    private fun insertRecords(tasks: List<TempCachePutTask>): Long {
        if (tasks.isEmpty()) {
            return -1
        }

        var database: SQLiteDatabase? = null
        var lastId = -1L
        try {
            database = dbConnector.openDb()
            database?.let { db ->
                db.beginTransaction()
                try {
                    for (task in tasks) {
                        val values = ContentValues().apply {
                            put(TempCacheTable.Column.SCOPE, task.scope)
                            put(TempCacheTable.Column.TIMESTAMP, task.timestamp)
                            put(TempCacheTable.Column.DATA, task.data)
                        }
                        lastId = db.insertOrThrow(tableName, null, values)
                        DebugLogger.info(
                            tag,
                            "Inserted record with scope = ${task.scope}; timestamp = ${task.timestamp}; " +
                                "data = array[${task.data.size}]. Id = $lastId"
                        )
                    }
                    db.setTransactionSuccessful()
                    DebugLogger.info(tag, "Successfully wrote ${tasks.size} records")
                } finally {
                    db.endTransaction()
                }
            }
        } catch (e: Throwable) {
            DebugLogger.error(tag, e, "Error writing ${tasks.size} records")
            lastId = -1
        } finally {
            dbConnector.closeDb(database)
        }
        return lastId
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

    fun flush() {
        bufferedWriter.flush()
    }

    fun flushAsync() {
        bufferedWriter.flushAsync()
    }
}
