package io.appmetrica.analytics.impl.db.constants.migrations

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript
import io.appmetrica.analytics.impl.EventSource
import io.appmetrica.analytics.impl.FirstOccurrenceStatus
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.event.DbEventModel
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter
import io.appmetrica.analytics.impl.db.protobuf.converter.DbSessionModelConverter
import io.appmetrica.analytics.impl.db.session.DbSessionModel
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode
import io.appmetrica.analytics.logger.internal.DebugLogger

internal class ComponentDatabaseUpgradeScriptToV112 : DatabaseScript() {

    private val sessionsMigrator = SessionsMigrator()
    private val eventsMigrator = EventsMigrator()

    override fun runScript(database: SQLiteDatabase) {
        sessionsMigrator.runScript(database)
        eventsMigrator.runScript(database)
    }

    internal class SessionsMigrator : DatabaseScript() {
        private val tag = "[ComponentDatabaseUpgradeScriptToV112]"

        private val oldSessionTable = "sessions"

        private val recordsToReadLimit = 200

        private val oldKeyId = "id"
        private val oldKeyStartTime = "start_time"
        private val oldKeyReportRequestParameter = "report_request_parameters"
        private val oldKeyServerTimeOffset = "server_time_offset"
        private val oldKeySessionType = "type"
        private val oldKeyObtainedBeforeFirstSync = "obtained_before_first_sync"

        private val dbSessionModelConverter = DbSessionModelConverter()

        override fun runScript(database: SQLiteDatabase) {
            DebugLogger.info(tag, "run session migrations...")
            val sessionsDump = read(database)
            DebugLogger.info(tag, "reading from old db ${sessionsDump.size} valid records finished.")
            drop(database)
            DebugLogger.info(tag, "old session table dropped.")
            create(database)
            DebugLogger.info(tag, "new session table created.")
            write(database, sessionsDump)
            DebugLogger.info(tag, "importing session records finished.")
        }

        private fun read(database: SQLiteDatabase): List<ContentValues> {
            val sessionsDump = ArrayList<ContentValues>()
            var cursor: Cursor? = null
            try {
                cursor = database.query(
                    oldSessionTable,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "$recordsToReadLimit"
                )
                while (cursor.moveToNext()) {
                    val model = readRecord(cursor)
                    if (model != null && isValid(model)) {
                        sessionsDump.add(dbSessionModelConverter.fromModel(model))
                    } else {
                        DebugLogger.info(tag, "Ignore null or invalid record with id = ${model?.id}")
                    }
                }
                DebugLogger.info(tag, "found ${sessionsDump.size} sessions from legacy db")
            } catch (e: Throwable) {
                DebugLogger.error(tag, e)
            } finally {
                cursor?.close()
            }
            return sessionsDump
        }

        private fun isValid(model: DbSessionModel): Boolean =
            model.id != null && model.id >= 0 &&
                model.type != null &&
                !model.reportRequestParameters.isNullOrEmpty() &&
                model.description.startTime != null && model.description.startTime > 0

        private fun readRecord(cursor: Cursor): DbSessionModel? = try {
            DbSessionModel(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(oldKeyId)),
                type = fromCode(cursor.getInt(cursor.getColumnIndexOrThrow(oldKeySessionType))),
                reportRequestParameters = cursor.getString(cursor.getColumnIndexOrThrow(oldKeyReportRequestParameter)),
                description = DbSessionModel.Description(
                    startTime = cursor.getLong(cursor.getColumnIndexOrThrow(oldKeyStartTime)),
                    serverTimeOffset = cursor.getLong(cursor.getColumnIndexOrThrow(oldKeyServerTimeOffset)),
                    obtainedBeforeFirstSynchronization =
                    cursor.getInt(cursor.getColumnIndexOrThrow(oldKeyObtainedBeforeFirstSync)) == 1
                )
            )
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
            null
        }

        private fun fromCode(type: Int): SessionType? = when (type) {
            SessionType.FOREGROUND.code -> SessionType.FOREGROUND
            SessionType.BACKGROUND.code -> SessionType.BACKGROUND
            else -> null
        }

        private fun drop(database: SQLiteDatabase) {
            val sql = "DROP TABLE IF EXISTS $oldSessionTable"
            DebugLogger.info(tag, "Execute sql: $sql")
            database.execSQL(sql)
        }

        private fun create(database: SQLiteDatabase) {
            DebugLogger.info(tag, "Execute sql: ${Constants.SessionTable.CREATE_TABLE}")
            database.execSQL(Constants.SessionTable.CREATE_TABLE)
        }

        private fun write(database: SQLiteDatabase, sessionsDump: List<ContentValues>) {
            sessionsDump.forEach {
                try {
                    database.insertOrThrow(
                        Constants.SessionTable.TABLE_NAME,
                        null,
                        it
                    )
                } catch (e: Throwable) {
                    DebugLogger.error(tag, e)
                }
            }
        }
    }

    internal class EventsMigrator : DatabaseScript() {
        private val tag = "[ComponentDatabaseUpgradeScriptToV112]"

        private val recordsLimit = 2000

        private val oldKeyNumberInSession = "number"
        private val oldKeyGlobalNumber = "global_number"
        private val oldKeyNumberOfType = "number_of_type"
        private val oldKeyName = "name"
        private val oldKeyValue = "value"
        private val oldKeyType = "type"
        private val oldKeyTime = "time"
        private val oldKeySessionId = "session_id"
        private val oldKeyErrorEnvironment = "error_environment"
        private val oldKeySessionType = "session_type"
        private val oldKeyAppEnvironment = "app_environment"
        private val oldKeyAppEnvironmentRevision = "app_environment_revision"
        private val oldKeyTruncated = "truncated"
        private val oldKeyCustomType = "custom_type"
        private val oldKeyEncryptingMode = "encrypting_mode"
        private val oldKeyProfileId = "profile_id"
        private val oldKeyFirstOccurrenceStatus = "first_occurrence_status"
        private val oldKeySource = "source"
        private val oldKeyAttributionIdChanged = "attribution_id_changed"
        private val oldKeyOpenId = "open_id"
        private val oldKeyExtras = "extras"

        private val oldTableName = "reports"

        private val converter = DbEventModelConverter()

        override fun runScript(database: SQLiteDatabase) {
            DebugLogger.info(tag, "run events migrations...")
            createNewTable(database)
            DebugLogger.info(tag, "new events table created")
            importRecords(database)
            DebugLogger.info(tag, "records import finished")
            drop(database)
            DebugLogger.info(tag, "old reports table dropped.")
        }

        private fun importRecords(database: SQLiteDatabase) {
            var readRecordsCount = 0
            var importedRecordsCount = 0
            var cursor: Cursor? = null
            try {
                cursor = database.query(
                    oldTableName,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "$recordsLimit"
                )
                while (cursor.moveToNext()) {
                    readRecordsCount += 1
                    val model = readSingleRecord(cursor)
                    if (model != null && isValid(model)) {
                        if (writeRecord(database, converter.fromModel(model))) {
                            importedRecordsCount += 1
                        }
                    } else {
                        DebugLogger.info(
                            tag,
                            "ignore invalid or null event with type = ${model?.type} and id = ${model?.globalNumber}"
                        )
                    }
                }
                DebugLogger.info(tag, "Read $readRecordsCount records; imported $importedRecordsCount records")
            } catch (e: Throwable) {
                DebugLogger.error(tag, e)
            } finally {
                cursor?.close()
            }
        }

        private fun readSingleRecord(cursor: Cursor): DbEventModel? = try {
            DbEventModel(
                session = cursor.getLong(cursor.getColumnIndexOrThrow(oldKeySessionId)),
                sessionType = sessionTypeByCode(cursor.getInt(cursor.getColumnIndexOrThrow(oldKeySessionType))),
                numberInSession = cursor.getLong(cursor.getColumnIndexOrThrow(oldKeyNumberInSession)),
                type = InternalEvents.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(oldKeyType))),
                globalNumber = cursor.getLong(cursor.getColumnIndexOrThrow(oldKeyGlobalNumber)),
                time = cursor.getLong(cursor.getColumnIndexOrThrow(oldKeyTime)),
                description = DbEventModel.Description(
                    customType = cursor.getInt(cursor.getColumnIndexOrThrow(oldKeyCustomType)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(oldKeyName)),
                    value = cursor.getString(cursor.getColumnIndexOrThrow(oldKeyValue)),
                    numberOfType = cursor.getLong(cursor.getColumnIndexOrThrow(oldKeyNumberOfType)),
                    locationInfo = null,
                    errorEnvironment = cursor.getString(cursor.getColumnIndexOrThrow(oldKeyErrorEnvironment)),
                    appEnvironment = cursor.getString(cursor.getColumnIndexOrThrow(oldKeyAppEnvironment)),
                    appEnvironmentRevision = cursor.getLong(cursor.getColumnIndexOrThrow(oldKeyAppEnvironmentRevision)),
                    truncated = cursor.getInt(cursor.getColumnIndexOrThrow(oldKeyTruncated)),
                    connectionType = null,
                    cellularConnectionType = null,
                    encryptingMode =
                    encryptingModeByCode(cursor.getInt(cursor.getColumnIndexOrThrow(oldKeyEncryptingMode))),
                    profileId = cursor.getString(cursor.getColumnIndexOrThrow(oldKeyProfileId)),
                    firstOccurrenceStatus = extractFirstOccurrenceStatus(cursor),
                    source = extractSource(cursor),
                    attributionIdChanged = extractAttributionIdChanged(cursor),
                    openId = extractOpenId(cursor),
                    extras = cursor.getColumnIndex(oldKeyExtras).let {
                        if (it < 0) {
                            null
                        } else {
                            cursor.getBlob(it)
                        }
                    }
                )
            )
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
            null
        }

        private fun extractFirstOccurrenceStatus(cursor: Cursor): FirstOccurrenceStatus? = try {
            firstOccurrenceStatusByCode(cursor.getInt(cursor.getColumnIndexOrThrow(oldKeyFirstOccurrenceStatus)))
        } catch (e: Throwable) {
            FirstOccurrenceStatus.UNKNOWN
        }
        private fun extractSource(cursor: Cursor): EventSource? = try {
            sourceByCode(cursor.getInt(cursor.getColumnIndexOrThrow(oldKeySource)))
        } catch (e: Throwable) {
            EventSource.NATIVE
        }

        private fun extractAttributionIdChanged(cursor: Cursor): Boolean = try {
            cursor.getInt(cursor.getColumnIndexOrThrow(oldKeyAttributionIdChanged)) == 1
        } catch (e: Throwable) {
            false
        }

        private fun extractOpenId(cursor: Cursor): Int = try {
            cursor.getInt(cursor.getColumnIndexOrThrow(oldKeyOpenId))
        } catch (e: Throwable) {
            -1
        }

        private fun sourceByCode(code: Int?): EventSource? = when (code) {
            EventSource.NATIVE.code -> EventSource.NATIVE
            EventSource.JS.code -> EventSource.JS
            else -> null
        }

        private fun firstOccurrenceStatusByCode(code: Int?): FirstOccurrenceStatus? = when (code) {
            FirstOccurrenceStatus.FIRST_OCCURRENCE.mStatusCode -> FirstOccurrenceStatus.FIRST_OCCURRENCE
            FirstOccurrenceStatus.NON_FIRST_OCCURENCE.mStatusCode -> FirstOccurrenceStatus.NON_FIRST_OCCURENCE
            else -> FirstOccurrenceStatus.UNKNOWN
        }

        private fun sessionTypeByCode(sessionType: Int?): SessionType? = when (sessionType) {
            SessionType.FOREGROUND.code -> SessionType.FOREGROUND
            SessionType.BACKGROUND.code -> SessionType.BACKGROUND
            else -> null
        }

        private fun encryptingModeByCode(code: Int?): EventEncryptionMode? = when (code) {
            EventEncryptionMode.NONE.modeId -> EventEncryptionMode.NONE
            EventEncryptionMode.AES_VALUE_ENCRYPTION.modeId -> EventEncryptionMode.AES_VALUE_ENCRYPTION
            EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER.modeId ->
                EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER
            else -> null
        }

        private fun isValid(dbEventModel: DbEventModel?): Boolean =
            dbEventModel?.session != null && dbEventModel.session >= 10_000_000_000 &&
                dbEventModel.sessionType != null &&
                dbEventModel.numberInSession != null && dbEventModel.numberInSession >= 0 &&
                dbEventModel.type != null && dbEventModel.type != InternalEvents.EVENT_TYPE_UNDEFINED &&
                dbEventModel.globalNumber != null && dbEventModel.globalNumber >= 0 &&
                dbEventModel.time != null && dbEventModel.time >= 0 &&
                (dbEventModel.description.numberOfType == null || dbEventModel.description.numberOfType >= 0) &&
                (dbEventModel.description.truncated == null || dbEventModel.description.truncated >= 0)

        private fun drop(database: SQLiteDatabase) {
            val dropSql = "DROP TABLE IF EXISTS $oldTableName"
            DebugLogger.info(tag, "Execute sql: $dropSql")
            database.execSQL(dropSql)
        }

        private fun createNewTable(database: SQLiteDatabase) {
            DebugLogger.info(tag, "Execute sql: ${Constants.EventsTable.CREATE_TABLE}")
            database.execSQL(Constants.EventsTable.CREATE_TABLE)
        }

        private fun writeRecord(database: SQLiteDatabase, record: ContentValues): Boolean =
            try {
                database.insertOrThrow(
                    Constants.EventsTable.TABLE_NAME,
                    null,
                    record
                ) >= 0
            } catch (e: Throwable) {
                DebugLogger.error(tag, e)
                false
            }
    }
}
