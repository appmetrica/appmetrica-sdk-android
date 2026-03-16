package io.appmetrica.analytics.impl.db.storage

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.buffering.DeferredBatchExecutor
import io.appmetrica.analytics.coreutils.internal.buffering.MapMergingBuffer
import io.appmetrica.analytics.coreutils.internal.parsing.ParseUtils
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper
import io.appmetrica.analytics.impl.db.connectors.DBConnector
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.constants.Constants.KeyValueTable.KeyValueTableEntry
import io.appmetrica.analytics.impl.db.constants.Constants.PreferencesTable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.Closeable

internal open class KeyValueTableDbHelper(
    private val tableName: String,
    private val dbConnector: DBConnector,
    private val executor: IHandlerExecutor
) : IKeyValueTableDbHelper, Closeable {

    private val values = mutableMapOf<String, Any>()
    private val lock = Any()

    private val tag = "[KeyValueTableDbHelper-($tableName)]"

    @Volatile
    private var initialized = false

    private val deferredExecutor = DeferredBatchExecutor(
        executor = executor,
        buffer = MapMergingBuffer<String, Any?>(),
        processor = { tasks ->
            applyChanges(tasks.first())
            DebugLogger.info(tag, "Call process lambda")
        },
        delayMillis = DEFAULT_WRITE_DELAY_MILLIS,
        tag = tag
    )

    init {
        executor.execute {
            synchronized(lock) {
                loadValues()
                initialized = true
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                (lock as Object).notifyAll()
            }
        }
    }

    private fun loadValues() {
        var db: SQLiteDatabase? = null
        try {
            db = dbConnector.openDb()
            db?.query(
                tableName,
                arrayOf(
                    KeyValueTableEntry.FIELD_KEY,
                    KeyValueTableEntry.FIELD_VALUE,
                    KeyValueTableEntry.FIELD_TYPE
                ),
                null, null, null, null, null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val key = cursor.getString(cursor.getColumnIndexOrThrow(KeyValueTableEntry.FIELD_KEY))
                    val value = cursor.getString(cursor.getColumnIndexOrThrow(KeyValueTableEntry.FIELD_VALUE))
                    val type = cursor.getInt(cursor.getColumnIndexOrThrow(KeyValueTableEntry.FIELD_TYPE))

                    if (!key.isNullOrEmpty()) {
                        val parsedValue: Any? = when (type) {
                            Constants.KeyValueTable.BOOL -> when (value) {
                                "true" -> true
                                "false" -> false
                                else -> null
                            }

                            Constants.KeyValueTable.INT -> ParseUtils.parseInt(value)
                            Constants.KeyValueTable.LONG -> ParseUtils.parseLong(value)
                            Constants.KeyValueTable.STRING -> value
                            Constants.KeyValueTable.FLOAT -> ParseUtils.parseFloat(value)
                            else -> null
                        }
                        parsedValue?.let { values[key] = it }
                    }
                }
            }
        } catch (e: Throwable) {
            DebugLogger.error(tag, e, "Smth was wrong while loading preference values.")
        } finally {
            dbConnector.closeDb(db)
        }
    }

    override fun flush() {
        deferredExecutor.flush()
    }

    override fun flushAsync() {
        deferredExecutor.flushAsync()
    }

    private fun applyChanges(modifiedCopy: Map<String, Any?>) {
        DebugLogger.info(tag, "Apply changes")
        val contentValues = modifiedCopy.map { (key, value) ->
            ContentValues().apply {
                put(KeyValueTableEntry.FIELD_KEY, key)
                when {
                    value === this@KeyValueTableDbHelper -> putNull(KeyValueTableEntry.FIELD_VALUE)
                    value is String -> {
                        put(KeyValueTableEntry.FIELD_VALUE, value)
                        put(KeyValueTableEntry.FIELD_TYPE, Constants.KeyValueTable.STRING)
                    }

                    value is Long -> {
                        put(KeyValueTableEntry.FIELD_VALUE, value)
                        put(KeyValueTableEntry.FIELD_TYPE, Constants.KeyValueTable.LONG)
                    }

                    value is Int -> {
                        put(KeyValueTableEntry.FIELD_VALUE, value)
                        put(KeyValueTableEntry.FIELD_TYPE, Constants.KeyValueTable.INT)
                    }

                    value is Boolean -> {
                        put(KeyValueTableEntry.FIELD_VALUE, value.toString())
                        put(KeyValueTableEntry.FIELD_TYPE, Constants.KeyValueTable.BOOL)
                    }

                    value is Float -> {
                        put(KeyValueTableEntry.FIELD_VALUE, value)
                        put(KeyValueTableEntry.FIELD_TYPE, Constants.KeyValueTable.FLOAT)
                    }

                    value == null -> {
                        // Null means removing. Do nothing
                    }

                    DEBUG_MODE -> {
                        throw UnsupportedOperationException("Unknown value: $value")
                    }
                }
            }
        }.toTypedArray()

        insertPreferences(contentValues)
    }

    private fun insertPreferences(values: Array<ContentValues>?) {
        values ?: return

        var db: SQLiteDatabase? = null
        try {
            db = dbConnector.openDb()
            db?.let {
                it.beginTransaction()
                try {
                    for (row in values) {
                        if (row.getAsString(KeyValueTableEntry.FIELD_VALUE) == null) {
                            val key = row.getAsString(KeyValueTableEntry.FIELD_KEY)
                            it.delete(tableName, PreferencesTable.DELETE_WHERE_KEY, arrayOf(key))
                            DebugLogger.info(tag, "remove preferences from db: $key")
                        } else {
                            it.insertWithOnConflict(tableName, null, row, SQLiteDatabase.CONFLICT_REPLACE)
                            DebugLogger.info(
                                tag,
                                "Write preferences in db: ${row.toString().replace("%", "%%")}"
                            )
                        }
                    }
                    it.setTransactionSuccessful()
                } finally {
                    Utils.endTransaction(it)
                }
            }
        } catch (exception: Throwable) {
            DebugLogger.error(
                tag,
                "Smth was wrong while inserting preferences into database.\n%s\n%s",
                exception,
                values
            )
        } finally {
            dbConnector.closeDb(db)
        }
    }

    override fun getString(key: String, defValue: String?): String? {
        val value = getValue(key)
        return when {
            value is String -> value
            DEBUG_MODE && value != null -> throw InvalidTypeException("String", key, value.javaClass.simpleName)
            else -> defValue
        }
    }

    override fun getInt(key: String, defValue: Int): Int {
        val value = getValue(key)
        return when {
            value is Int -> value
            DEBUG_MODE && value != null -> throw InvalidTypeException("Integer", key, value.javaClass.simpleName)
            else -> defValue
        }
    }

    override fun getLong(key: String, defValue: Long): Long {
        val value = getValue(key)
        return when {
            value is Long -> value
            DEBUG_MODE && value != null -> throw InvalidTypeException("Long", key, value.javaClass.simpleName)
            else -> defValue
        }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        val value = getValue(key)
        return when {
            value is Boolean -> value
            DEBUG_MODE && value != null -> throw InvalidTypeException("Boolean", key, value.javaClass.simpleName)
            else -> defValue
        }
    }

    override fun getFloat(key: String, defValue: Float): Float {
        val value = getValue(key)
        return when {
            value is Float -> value
            DEBUG_MODE && value != null -> throw InvalidTypeException("Float", key, value.javaClass.simpleName)
            else -> defValue
        }
    }

    override fun remove(key: String): IKeyValueTableDbHelper {
        synchronized(lock) {
            waitForInit()
            values.remove(key)
        }
        // Submit deletion marker (this object) to the deferred executor
        deferredExecutor.submit(mapOf(key to this))
        return this
    }

    override fun put(key: String, value: String): IKeyValueTableDbHelper {
        putValue(key, value)
        return this
    }

    override fun put(key: String, value: Long): IKeyValueTableDbHelper {
        putValue(key, value)
        return this
    }

    override fun put(key: String, value: Int): IKeyValueTableDbHelper {
        putValue(key, value)
        return this
    }

    override fun put(key: String, value: Boolean): IKeyValueTableDbHelper {
        putValue(key, value)
        return this
    }

    override fun put(key: String, value: Float): IKeyValueTableDbHelper {
        putValue(key, value)
        return this
    }

    override fun containsKey(key: String): Boolean {
        synchronized(lock) {
            waitForInit()
            return values.containsKey(key)
        }
    }

    override fun keys(): Set<String> {
        synchronized(lock) {
            return HashSet(values.keys)
        }
    }

    @VisibleForTesting
    fun putValue(key: String, value: Any) {
        synchronized(lock) {
            waitForInit()
            values[key] = value
        }
        deferredExecutor.submit(mapOf(key to value))
    }

    private fun getValue(key: String): Any? {
        synchronized(lock) {
            waitForInit()
            return values[key]
        }
    }

    private fun waitForInit() {
        if (!initialized) {
            try {
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                (lock as Object).wait()
            } catch (e: InterruptedException) {
                // Ignore
            }
        }
    }

    @VisibleForTesting
    override fun close() {
        flush() // Ensure all pending changes are written
    }

    internal class InvalidTypeException(expected: String, key: String, actualType: String) :
        RuntimeException("$expected expected, but key $key has value of type $actualType")

    companion object {
        private const val DEBUG_MODE = BuildConfig.METRICA_DEBUG
        private const val DEFAULT_WRITE_DELAY_MILLIS = 1000L
    }
}
