package io.appmetrica.analytics.impl.service.migration

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript
import io.appmetrica.analytics.impl.MigrationManager.MigrationScript
import io.appmetrica.analytics.impl.db.TablesManager
import io.appmetrica.analytics.impl.db.TablesValidator
import io.appmetrica.analytics.impl.db.connectors.SimpleDBConnector
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.state.factory.StorageFactory
import io.appmetrica.analytics.impl.db.storage.BinaryDataHelper
import io.appmetrica.analytics.impl.db.storage.DatabaseSimpleNameProvider
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory
import io.appmetrica.analytics.impl.startup.StartupStateModel
import io.appmetrica.analytics.impl.utils.collection.HashMultimap
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ServiceMigrationScriptToV116 : MigrationScript {

    private val tag = "[ServiceMigrationScriptToV116]"

    override fun run(context: Context) {
        DebugLogger.info(tag, "Run migration")
        val storageFactory = StorageFactory.Provider.get(StartupStateModel::class.java).createForMigration(context)
        storageFactory.save(
            storageFactory
                .read()
                .buildUpon()
                .withObtainTime(0L)
                .build()
        )
        migrateInappStatFromSeparateToServiceStorage(context)
    }

    private fun migrateInappStatFromSeparateToServiceStorage(context: Context) {
        val nameProvider = object : DatabaseSimpleNameProvider {
            override val databaseName: String
                get() = "auto_inapp.db"
            override val legacyDatabaseName: String
                get() = "metrica_aip.db"
        }

        val databaseScriptStub = object : DatabaseScript() {
            override fun runScript(database: SQLiteDatabase) {
                DebugLogger.info(tag, "Ignore script for old migration")
            }
        }

        val tablesValidatorStub = TablesValidator { true }

        val tablesManager = TablesManager.Creator().createTablesManager(
            "autoinapp-old",
            databaseScriptStub,
            databaseScriptStub,
            HashMultimap(),
            tablesValidatorStub
        )

        val datatabaseStorage = DatabaseStorageFactory.getInstance(context).createLegacyStorageForMigration(
            "autoinapp-old",
            nameProvider,
            tablesManager
        )
        val dbHelper = BinaryDataHelper(SimpleDBConnector(datatabaseStorage), Constants.BinaryDataTable.TABLE_NAME)
        val oldValue = dbHelper.get("auto_inapp_collecting_info_data")
        if (oldValue != null) {
            DebugLogger.info(tag, "Migrate old inapp data (length: ${oldValue.size})")
            DatabaseStorageFactory.getInstance(context).serviceBinaryDataHelperForMigration.insert(
                "auto_inapp_collecting_info_data",
                oldValue
            )
        } else {
            DebugLogger.info(tag, "Not found old auto inapp data")
        }
    }
}
