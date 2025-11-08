package io.appmetrica.analytics.impl.service.migration

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter
import io.appmetrica.analytics.coreutils.internal.io.closeSafely
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.MigrationManager.MigrationScript
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory
import io.appmetrica.analytics.impl.protobuf.client.LegacyStartupStateProtobuf.LegacyStartupState
import io.appmetrica.analytics.impl.startup.CollectingFlags
import io.appmetrica.analytics.impl.startup.StartupStateModel
import io.appmetrica.analytics.impl.utils.encryption.AESCredentialProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ServiceMigrationScriptToV112(private val vitalDataStorage: VitalCommonDataProvider) : MigrationScript {

    private val tag = "[ServiceMigrationScriptToV112]"
    private val startupStateKey = "startup_state"

    private val aesEncrypter: AESEncrypter = with(AESCredentialProvider(GlobalServiceLocator.getInstance().context)) {
        AESEncrypter(
            AESEncrypter.DEFAULT_ALGORITHM,
            this.password,
            this.iv
        )
    }

    override fun run(context: Context) {
        DebugLogger.info(tag, "Run migration")
        GlobalServiceLocator.getInstance().storageFactory.getStorageForService(context)
            .readableDatabase?.let { database ->
                DebugLogger.info(tag, "Legacy database exists... Try to import data")
                try {
                    val legacyStartupState = readLegacyStartupState(database)
                    val startupStateModelBuilder =
                        StartupStateModel.StartupStateBuilder(CollectingFlags.CollectingFlagsBuilder().build())
                    if (legacyStartupState != null) {
                        fillIdentifiersToStartupState(vitalDataStorage, startupStateModelBuilder, legacyStartupState)
                        DebugLogger.info(
                            tag,
                            "Import country init info: hadFirstStartup = ${legacyStartupState.hadFirstStartup}; " +
                                "countryInit = ${legacyStartupState.countryInit}"
                        )
                        startupStateModelBuilder.withHadFirstStartup(legacyStartupState.hadFirstStartup)
                            .withCountryInit(legacyStartupState.countryInit)
                    } else {
                        DebugLogger.info(tag, "Legacy startup state missing or invalid")
                    }
                    val startupStateModel = startupStateModelBuilder.build()
                    DebugLogger.info(tag, "write startup: $startupStateModel")
                    StorageFactory.Provider.get(StartupStateModel::class.java).createForMigration(context)
                        .save(startupStateModel)
                } catch (e: Throwable) {
                    DebugLogger.error(tag, e)
                }
            }
    }

    private fun fillIdentifiersToStartupState(
        vitalDataStorage: VitalCommonDataProvider,
        startupStateBuilder: StartupStateModel.StartupStateBuilder,
        legacyStartupState: LegacyStartupState
    ) {
        DebugLogger.info(tag, "Import identifiers from legacy storage...")
        val actualDeviceId = vitalDataStorage.deviceId
        if (TextUtils.isEmpty(actualDeviceId)) {
            if (!TextUtils.isEmpty(legacyStartupState.deviceId)) {
                vitalDataStorage.deviceId = legacyStartupState.deviceId
                DebugLogger.info(tag, "Imported deviceId = ${legacyStartupState.deviceId}")
            } else {
                DebugLogger.info(tag, "DeviceId not found")
            }
            if (!TextUtils.isEmpty(legacyStartupState.deviceIdHash)) {
                vitalDataStorage.deviceIdHash = legacyStartupState.deviceIdHash
                DebugLogger.info(tag, "Imported deviceIdHash = ${legacyStartupState.deviceIdHash}")
            } else {
                DebugLogger.info(tag, "DeviceIdHash not found")
            }
            if (!TextUtils.isEmpty(legacyStartupState.uuid)) {
                startupStateBuilder.withUuid(legacyStartupState.uuid)
                DebugLogger.info(tag, "Successfully imported uuid = ${legacyStartupState.uuid}")
            } else {
                DebugLogger.info(tag, "Uuid not found")
            }
        } else {
            DebugLogger.info(
                tag,
                "Device id presents in actual storage. Use values from vital storage: " +
                    "deviceId = ${vitalDataStorage.deviceId}; deviceIdHash = ${vitalDataStorage.deviceIdHash}"
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
                    DebugLogger.error(
                        tag,
                        "invalid cursor for key ${Constants.BinaryDataTable.DATA_KEY} " +
                            "from ${Constants.BinaryDataTable.TABLE_NAME}"
                    )
                } else {
                    DebugLogger.info(
                        tag,
                        "value for key ${Constants.BinaryDataTable.DATA_KEY} = $startupStateKey " +
                            "from ${Constants.BinaryDataTable.TABLE_NAME} is empty."
                    )
                }
            }
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
        } finally {
            cursor.closeSafely()
        }
        return null
    }

    private fun parseBytes(bytes: ByteArray): LegacyStartupState {
        DebugLogger.info(tag, "encrypted legacy startup size = ${bytes.size}")
        val result = aesEncrypter.decrypt(bytes)
        DebugLogger.info(tag, "legacy startup size = ${result?.size}")
        return LegacyStartupState.parseFrom(result)
    }
}
