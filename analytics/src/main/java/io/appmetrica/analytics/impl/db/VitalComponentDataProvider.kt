package io.appmetrica.analytics.impl.db

import androidx.annotation.WorkerThread
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optJsonObjectOrNull
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optJsonObjectOrNullable
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

private const val FIRST_EVENT_DONE = "first_event_done"
private const val INIT_EVENT_DONE = "init_event_done"
private const val KEY_REPORT_REQUEST_ID = "report_request_id"
private const val KEY_GLOBAL_NUMBER = "global_number"
private const val KEY_NUMBERS_OF_TYPE = "numbers_of_type"
private const val KEY_SESSION_ID = "session_id"
private const val KEY_REFERRER_HANDLED = "referrer_handled"
private const val KEY_OPEN_ID = "open_id"
private const val KEY_ATTRIBUTION_ID = "attribution_id"
private const val KEY_LAST_MIGRATION_API_LEVEL = "last_migration_api_level"
private const val KEY_EXTERNAL_ATTRIBUTION_WINDOW_START = "external_attribution_window_start"

private const val DEFAULT_HAS_FIRST = false
private const val DEFAULT_HAS_INIT = false
private const val DEFAULT_REPORT_REQUEST_ID = -1
private const val DEFAULT_GLOBAL_NUMBER = 0L
private const val DEFAULT_SESSION_ID = -1L
private const val DEFAULT_REFERRER_HANDLED = false
private const val DEFAULT_OPEN_ID = 1
private const val DEFAULT_ATTRIBUTION_ID = 1
private const val DEFAULT_LAST_MIGRATION_API_LEVEL = 0
private const val DEFAULT_EXTERNAL_ATTRIBUTION_WINDOW_START = -1L

internal class VitalComponentDataProvider(
    primaryDataSource: VitalDataSource,
    backupDataSource: VitalDataSource,
    apiKey: String?
) {
    private val vitalDataProvider: VitalDataProvider = VitalDataProvider(
        primaryDataSource,
        backupDataSource,
        "[VitalComponentDataProvider-$apiKey]"
    ) { primary, backup ->
        JSONObject().apply {
            put(
                FIRST_EVENT_DONE,
                JsonHelper.optBooleanOrDefaultWithBackup(primary, backup, FIRST_EVENT_DONE, DEFAULT_HAS_FIRST)
            )
            put(
                INIT_EVENT_DONE,
                JsonHelper.optBooleanOrDefaultWithBackup(primary, backup, INIT_EVENT_DONE, DEFAULT_HAS_INIT)
            )
            put(
                KEY_REPORT_REQUEST_ID,
                JsonHelper.optIntegerOrDefaultWithBackup(
                    primary, backup, KEY_REPORT_REQUEST_ID, DEFAULT_REPORT_REQUEST_ID
                )
            )
            put(
                KEY_GLOBAL_NUMBER,
                JsonHelper.optLongOrDefaultWithBackup(primary, backup, KEY_GLOBAL_NUMBER, DEFAULT_GLOBAL_NUMBER)
            )
            put(
                KEY_SESSION_ID,
                JsonHelper.optLongOrDefaultWithBackup(primary, backup, KEY_SESSION_ID, DEFAULT_SESSION_ID)
            )
            put(
                KEY_REFERRER_HANDLED,
                JsonHelper.optBooleanOrDefaultWithBackup(
                    primary, backup, KEY_REFERRER_HANDLED, DEFAULT_REFERRER_HANDLED
                )
            )
            put(
                KEY_NUMBERS_OF_TYPE,
                backup.optJsonObjectOrNullable(
                    KEY_NUMBERS_OF_TYPE,
                    primary.optJsonObjectOrNull(KEY_NUMBERS_OF_TYPE)
                )
            )
            put(
                KEY_OPEN_ID,
                JsonHelper.optIntegerOrDefaultWithBackup(primary, backup, KEY_OPEN_ID, DEFAULT_OPEN_ID)
            )
            put(
                KEY_ATTRIBUTION_ID,
                JsonHelper.optIntegerOrDefaultWithBackup(primary, backup, KEY_ATTRIBUTION_ID, DEFAULT_ATTRIBUTION_ID)
            )
            put(
                KEY_LAST_MIGRATION_API_LEVEL,
                JsonHelper.optIntegerOrDefaultWithBackup(
                    primary, backup, KEY_LAST_MIGRATION_API_LEVEL, DEFAULT_LAST_MIGRATION_API_LEVEL
                )
            )
            put(
                KEY_EXTERNAL_ATTRIBUTION_WINDOW_START,
                JsonHelper.optLongOrDefaultWithBackup(
                    primary, backup, KEY_EXTERNAL_ATTRIBUTION_WINDOW_START, DEFAULT_EXTERNAL_ATTRIBUTION_WINDOW_START
                )
            )
        }
    }

    var isFirstEventDone: Boolean
        @WorkerThread @Synchronized get() =
            vitalDataProvider.getOrLoadData().optBoolean(FIRST_EVENT_DONE, DEFAULT_HAS_FIRST)
        @WorkerThread @Synchronized set(value) {
            vitalDataProvider.save(vitalDataProvider.getOrLoadData().put(FIRST_EVENT_DONE, value))
        }
    var isInitEventDone: Boolean
        @WorkerThread @Synchronized get() =
            vitalDataProvider.getOrLoadData().optBoolean(INIT_EVENT_DONE, DEFAULT_HAS_INIT)
        @WorkerThread @Synchronized set(value) {
            vitalDataProvider.save(vitalDataProvider.getOrLoadData().put(INIT_EVENT_DONE, value))
        }
    var reportRequestId: Int
        @WorkerThread @Synchronized get() =
            vitalDataProvider.getOrLoadData().optInt(KEY_REPORT_REQUEST_ID, DEFAULT_REPORT_REQUEST_ID)
        @WorkerThread @Synchronized set(value) {
            vitalDataProvider.save(vitalDataProvider.getOrLoadData().put(KEY_REPORT_REQUEST_ID, value))
        }
    var globalNumber: Long
        @WorkerThread @Synchronized get() =
            vitalDataProvider.getOrLoadData().optLong(KEY_GLOBAL_NUMBER, DEFAULT_GLOBAL_NUMBER)
        @WorkerThread @Synchronized set(value) {
            vitalDataProvider.save(vitalDataProvider.getOrLoadData().put(KEY_GLOBAL_NUMBER, value))
        }
    var sessionId: Long
        @WorkerThread @Synchronized get() =
            vitalDataProvider.getOrLoadData().optLong(KEY_SESSION_ID, DEFAULT_SESSION_ID)
        @WorkerThread @Synchronized set(value) {
            vitalDataProvider.save(vitalDataProvider.getOrLoadData().put(KEY_SESSION_ID, value))
        }
    var referrerHandled: Boolean
        @WorkerThread @Synchronized get() =
            vitalDataProvider.getOrLoadData().optBoolean(KEY_REFERRER_HANDLED, DEFAULT_REFERRER_HANDLED)
        @WorkerThread @Synchronized set(value) {
            vitalDataProvider.save(vitalDataProvider.getOrLoadData().put(KEY_REFERRER_HANDLED, value))
        }
    var numbersOfType: JSONObject?
        @WorkerThread @Synchronized get() = vitalDataProvider.getOrLoadData().optJSONObject(KEY_NUMBERS_OF_TYPE)
        @WorkerThread @Synchronized set(value) {
            vitalDataProvider.save(vitalDataProvider.getOrLoadData().put(KEY_NUMBERS_OF_TYPE, value))
        }
    var openId: Int
        @WorkerThread @Synchronized get() = vitalDataProvider.getOrLoadData().optInt(KEY_OPEN_ID, DEFAULT_OPEN_ID)
        @WorkerThread @Synchronized private set(value) {
            vitalDataProvider.save(vitalDataProvider.getOrLoadData().put(KEY_OPEN_ID, value))
        }
    var attributionId: Int
        @WorkerThread @Synchronized get() =
            vitalDataProvider.getOrLoadData().optInt(KEY_ATTRIBUTION_ID, DEFAULT_ATTRIBUTION_ID)
        @WorkerThread @Synchronized private set(value) {
            DebugLogger.info("[VitalComponentDataProvider]", "Save attributionId = $value")
            vitalDataProvider.save(vitalDataProvider.getOrLoadData().put(KEY_ATTRIBUTION_ID, value))
        }
    var lastMigrationApiLevel: Int
        @WorkerThread @Synchronized get() =
            vitalDataProvider.getOrLoadData().optInt(KEY_LAST_MIGRATION_API_LEVEL, DEFAULT_LAST_MIGRATION_API_LEVEL)
        @WorkerThread @Synchronized set(value) {
            vitalDataProvider.save(vitalDataProvider.getOrLoadData().put(KEY_LAST_MIGRATION_API_LEVEL, value))
        }
    var externalAttributionWindowStart: Long
        @WorkerThread @Synchronized get() = vitalDataProvider.getOrLoadData()
            .optLong(KEY_EXTERNAL_ATTRIBUTION_WINDOW_START, DEFAULT_EXTERNAL_ATTRIBUTION_WINDOW_START)
        @WorkerThread @Synchronized set(value) {
            vitalDataProvider.save(vitalDataProvider.getOrLoadData().put(KEY_EXTERNAL_ATTRIBUTION_WINDOW_START, value))
        }

    @Synchronized
    @WorkerThread
    fun incrementOpenId() {
        openId += 1
    }

    @Synchronized
    @WorkerThread
    fun incrementAttributionId() {
        attributionId += 1
    }

    @WorkerThread
    @Synchronized
    fun setInitialState(
        isFirstEventDone: Boolean,
        isInitEventDone: Boolean,
        reportRequestId: Int?,
        globalNumber: Int?,
        sessionId: Long?,
        referrerHandled: Boolean?,
        numbersOfType: JSONObject?,
        openId: Int?,
        attributionId: Int?,
        lastMigrationApiLevel: Int?,
        externalAttributionWindowStart: Long?
    ) {
        val json = JSONObject()
            .put(FIRST_EVENT_DONE, isFirstEventDone)
            .put(INIT_EVENT_DONE, isInitEventDone)
            .put(KEY_REPORT_REQUEST_ID, reportRequestId)
            .put(KEY_GLOBAL_NUMBER, globalNumber)
            .put(KEY_SESSION_ID, sessionId)
            .put(KEY_REFERRER_HANDLED, referrerHandled)
            .put(KEY_OPEN_ID, openId)
            .put(KEY_ATTRIBUTION_ID, attributionId)
            .put(KEY_NUMBERS_OF_TYPE, numbersOfType)
            .put(KEY_LAST_MIGRATION_API_LEVEL, lastMigrationApiLevel)
            .put(KEY_EXTERNAL_ATTRIBUTION_WINDOW_START, externalAttributionWindowStart)
        vitalDataProvider.save(json)
    }
}
