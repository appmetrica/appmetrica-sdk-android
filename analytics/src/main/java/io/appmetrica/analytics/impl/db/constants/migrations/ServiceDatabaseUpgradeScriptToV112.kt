package io.appmetrica.analytics.impl.db.constants.migrations

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter
import io.appmetrica.analytics.coreutils.internal.io.closeSafely
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.protobuf.client.LegacyStartupStateProtobuf.LegacyStartupState
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.impl.utils.encryption.AESCredentialProvider
import io.appmetrica.analytics.protobuf.nano.MessageNano

internal class ServiceDatabaseUpgradeScriptToV112 : DatabaseScript() {

    private val tag = "[ServiceDatabaseUpgradeScriptToV112]"
    private val startupStateKey = "startup_state"

    private val aesEncrypter: AESEncrypter = with(AESCredentialProvider(GlobalServiceLocator.getInstance().context)) {
        AESEncrypter(
            AESEncrypter.DEFAULT_ALGORITHM,
            this.password,
            this.iv
        )
    }

    override fun runScript(database: SQLiteDatabase) {
        try {
            val legacyStartupState = readLegacyStartupState(database)
            val startupState = StartupStateProtobuf.StartupState()
            startupState.flags = StartupStateProtobuf.StartupState.Flags()

            if (legacyStartupState != null) {
                if (!TextUtils.isEmpty(legacyStartupState.deviceId)) {
                    YLogger.info(tag, "Successfully imported deviceId from legacy startup state")
                    startupState.deviceID = legacyStartupState.deviceId
                } else {
                    YLogger.info(tag, "DeviceId not found")
                }
                if (!TextUtils.isEmpty(legacyStartupState.deviceIdHash)) {
                    YLogger.info(tag, "Successfully imported deivceIdHash from legacy startup state")
                    startupState.deviceIDHash = legacyStartupState.deviceIdHash
                } else {
                    YLogger.info(tag, "DeviceIdHash not found")
                }
                if (!TextUtils.isEmpty(legacyStartupState.uuid)) {
                    YLogger.info(tag, "Successfully imported uuid from legacy startup state")
                    startupState.uuid = legacyStartupState.uuid
                } else {
                    YLogger.info(tag, "Uuid not found")
                }
                YLogger.info(tag, "Import country init info")
                startupState.hadFirstStartup = legacyStartupState.hadFirstStartup
                startupState.countryInit = legacyStartupState.countryInit
            } else {
                YLogger.info(tag, "Legacy startup state missing or invalid")
            }
            writeStartupState(database, startupState)
        } catch (e: Throwable) {
            YLogger.error(tag, e)
        }
    }

    private fun writeStartupState(database: SQLiteDatabase, startupState: StartupStateProtobuf.StartupState) {
        try {
            val values = ContentValues()
            val startupStateBytes = MessageNano.toByteArray(startupState)
            YLogger.info(tag, "startup state bytes size = ${startupStateBytes.size}")
            val encryptedBytes = aesEncrypter.encrypt(startupStateBytes)
            YLogger.info(tag, "encrypted startup state bytes size = ${encryptedBytes.size}")
            values.put(Constants.BinaryDataTable.DATA_KEY, startupStateKey)
            values.put(Constants.BinaryDataTable.VALUE, encryptedBytes)
            database.insertWithOnConflict(
                Constants.BinaryDataTable.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
        } catch (e: Throwable) {
            YLogger.error(
                tag,
                "could not insert ${Constants.BinaryDataTable.DATA_KEY} into ${Constants.BinaryDataTable.TABLE_NAME}",
            )
        }
    }

    private fun readLegacyStartupState(database: SQLiteDatabase): LegacyStartupState? {
        var cursor: Cursor? = null
        try {
            cursor = database.query(
                Constants.BinaryDataTable.TABLE_NAME,
                arrayOf(Constants.BinaryDataTable.VALUE),
                Constants.BinaryDataTable.DATA_KEY + " = ?",
                arrayOf(startupStateKey),
                null,
                null,
                null
            )
            if (cursor != null && cursor.count == 1 && cursor.moveToFirst()) {
                val binaryData = cursor.getBlob(cursor.getColumnIndexOrThrow(Constants.BinaryDataTable.VALUE))
                return parseBytes(binaryData)
            } else {
                if (Utils.isNullOrEmpty(cursor) == false) {
                    YLogger.error(
                        tag,
                        "invalid cursor for key ${Constants.BinaryDataTable.DATA_KEY} " +
                            "from ${Constants.BinaryDataTable.TABLE_NAME}"
                    )
                } else {
                    YLogger.info(
                        tag,
                        "database for key ${Constants.BinaryDataTable.DATA_KEY} " +
                            "from ${Constants.BinaryDataTable.TABLE_NAME} is empty.",
                    )
                }
            }
        } catch (e: Throwable) {
            YLogger.error(tag, e)
        } finally {
            cursor.closeSafely()
        }
        return null
    }

    private fun parseBytes(bytes: ByteArray): LegacyStartupState {
        YLogger.info(tag, "encrypted legacy startup size = ${bytes.size}")
        val result = aesEncrypter.decrypt(bytes)
        YLogger.info(tag, "legacy startup size = ${result.size}")
        return LegacyStartupState.parseFrom(result)
    }
}
