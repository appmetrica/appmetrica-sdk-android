package io.appmetrica.analytics.impl.db

import android.util.Base64
import androidx.annotation.WorkerThread
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optStringOrNull
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

private const val KEY_DEVICE_ID = "device_id"
private const val KEY_DEVICE_ID_HASH = "device_id_hash"
private const val KEY_REFERRER = "referrer"
private const val KEY_REFERRER_CHECKED = "referrer_checked"
private const val KEY_LAST_MIGRATION_API_LEVEL = "last_migration_api_level"

private const val DEFAULT_REFERRER_CHECKED = false
private const val DEFAULT_LAST_MIGRATION_API_LEVEL = -1

internal class VitalCommonDataProvider(
    primaryDataSource: VitalDataSource,
    backupDataSource: VitalDataSource,
) {
    private val tag = "[VitalCommonDataProvider]"

    companion object {
        // If the data storage format and location change, you must notify https://nda.ya.ru/t/94XNTaaf7LkVFu
        const val BACKUP_FILE_NAME = "appmetrica_vital.dat"
    }

    private val vitalDataProvider: VitalDataProvider = VitalDataProvider(
        primaryDataSource,
        backupDataSource,
        tag
    ) { primary, backup ->
        // If the data storage format and location change, you must notify https://nda.ya.ru/t/94XNTaaf7LkVFu
        JSONObject().apply {
            put(KEY_DEVICE_ID, JsonHelper.optStringOrDefaultWithBackup(primary, backup, KEY_DEVICE_ID))
            put(KEY_DEVICE_ID_HASH, JsonHelper.optStringOrDefaultWithBackup(primary, backup, KEY_DEVICE_ID_HASH))
            put(KEY_REFERRER, JsonHelper.optStringOrDefaultWithBackup(primary, backup, KEY_REFERRER))
            put(
                KEY_REFERRER_CHECKED,
                JsonHelper.optBooleanOrDefaultWithBackup(
                    primary, backup, KEY_REFERRER_CHECKED, DEFAULT_REFERRER_CHECKED
                )
            )
            put(
                KEY_LAST_MIGRATION_API_LEVEL,
                JsonHelper.optIntegerOrDefaultWithBackup(
                    primary,
                    backup,
                    KEY_LAST_MIGRATION_API_LEVEL,
                    DEFAULT_LAST_MIGRATION_API_LEVEL
                )
            )
        }
    }

    // If the data storage format and location change, you must notify https://nda.ya.ru/t/94XNTaaf7LkVFu
    var deviceId: String?
        @WorkerThread @Synchronized get() = vitalDataProvider.getOrLoadData().optStringOrNull(KEY_DEVICE_ID)
        @WorkerThread @Synchronized set(value) {
            updateStringIfChanged(KEY_DEVICE_ID, value)
            vitalDataProvider.flushAsync()
        }

    // If the data storage format and location change, you must notify https://nda.ya.ru/t/94XNTaaf7LkVFu
    var deviceIdHash: String?
        @WorkerThread @Synchronized get() = vitalDataProvider.getOrLoadData().optStringOrNull(KEY_DEVICE_ID_HASH)
        @WorkerThread @Synchronized set(value) {
            updateStringIfChanged(KEY_DEVICE_ID_HASH, value)
            vitalDataProvider.flushAsync()
        }

    var referrer: ReferrerInfo?
        @WorkerThread @Synchronized get() =
            vitalDataProvider.getOrLoadData().optStringOrNull(KEY_REFERRER)?.toReferrerInfo()
        @WorkerThread @Synchronized set(value) {
            val encodedValue = value?.toEncodedString()
            updateStringIfChanged(KEY_REFERRER, encodedValue)
            vitalDataProvider.flushAsync()
        }
    var referrerChecked: Boolean
        @WorkerThread @Synchronized get() =
            vitalDataProvider.getOrLoadData().optBoolean(KEY_REFERRER_CHECKED, DEFAULT_REFERRER_CHECKED)
        @WorkerThread @Synchronized set(value) {
            updateBooleanIfChanged(KEY_REFERRER_CHECKED, value, DEFAULT_REFERRER_CHECKED)
            vitalDataProvider.flushAsync()
        }

    var lastMigrationApiLevel: Int
        @WorkerThread @Synchronized get() =
            vitalDataProvider.getOrLoadData().optInt(KEY_LAST_MIGRATION_API_LEVEL, DEFAULT_LAST_MIGRATION_API_LEVEL)
        @WorkerThread @Synchronized set(value) {
            updateIntIfChanged(KEY_LAST_MIGRATION_API_LEVEL, value, DEFAULT_LAST_MIGRATION_API_LEVEL)
            vitalDataProvider.flushAsync()
        }

    @WorkerThread
    private fun updateStringIfChanged(key: String, value: String?) {
        val data = vitalDataProvider.getOrLoadData()
        if (data.optStringOrNull(key) == value) return
        vitalDataProvider.save(data.put(key, value))
    }

    @WorkerThread
    private fun updateBooleanIfChanged(key: String, value: Boolean, default: Boolean) {
        val data = vitalDataProvider.getOrLoadData()
        if (data.optBoolean(key, default) == value) return
        vitalDataProvider.save(data.put(key, value))
    }

    @WorkerThread
    private fun updateIntIfChanged(key: String, value: Int, default: Int) {
        val data = vitalDataProvider.getOrLoadData()
        if (data.optInt(key, default) == value) return
        vitalDataProvider.save(data.put(key, value))
    }

    @WorkerThread
    @Synchronized
    fun init() {
        vitalDataProvider.getOrLoadData()
    }

    @Synchronized
    fun flush() {
        vitalDataProvider.flush()
    }

    fun flushAsync() {
        vitalDataProvider.flushAsync()
    }

    @WorkerThread
    @Synchronized
    fun setInitialState(
        deviceId: String?,
        deviceIdHash: String?,
        referrerInfo: String?,
        referrerChecked: Boolean?,
        lastMigrationApiLevel: Int?
    ) {
        val json = JSONObject()
            .put(KEY_DEVICE_ID, deviceId)
            .put(KEY_DEVICE_ID_HASH, deviceIdHash)
            .put(KEY_REFERRER, referrerInfo)
            .put(KEY_REFERRER_CHECKED, referrerChecked)
            .put(KEY_LAST_MIGRATION_API_LEVEL, lastMigrationApiLevel)
        vitalDataProvider.save(json)
    }

    private fun String.toReferrerInfo(): ReferrerInfo? {
        return try {
            ReferrerInfo.parseFrom(Base64.decode(toByteArray(), 0))
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex)
            null
        }
    }

    private fun ReferrerInfo.toEncodedString(): String {
        return String(Base64.encode(toProto(), 0))
    }
}
