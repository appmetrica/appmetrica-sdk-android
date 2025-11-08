package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper
import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.DatabaseStorage
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper
import io.appmetrica.analytics.impl.db.StorageType
import io.appmetrica.analytics.impl.db.TablesManager
import io.appmetrica.analytics.impl.db.connectors.SimpleDBConnector
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.constants.TempCacheTable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.File

internal class ServiceStorageFactory(outerStorageDirectory: File?) {

    private val tag = "[ServiceStorageFactory]"

    private val databaseStoragePathProviderFactory = DatabaseStoragePathProviderFactory(outerStorageDirectory)

    private var serviceDatabaseStorage: DatabaseStorage? = null
    private var serviceBinaryDataHelper: IBinaryDataHelper? = null
    private var serviceBinaryDataHelperWrapper: IBinaryDataHelper? = null
    private var servicePreferenceDbHelper: IKeyValueTableDbHelper? = null
    private var servicePreferenceDbHelperWrapper: IKeyValueTableDbHelper? = null
    private var rawServiceTempCacheStorage: TempCacheStorage? = null
    private var serviceTempCacheStorage: TempCacheStorage? = null
    private val databaseStorages = mutableMapOf<String, DatabaseStorage>()
    private val databaseHelpers = mutableMapOf<String, IKeyValueTableDbHelper>()
    private val componentBinaryDbHelpers = mutableMapOf<String, IBinaryDataHelper>()

    @Synchronized
    fun getStorageForService(context: Context): DatabaseStorage {
        return serviceDatabaseStorage ?: DatabaseStorage(
            context,
            databaseStoragePathProviderFactory.create("service", true)
                .getPath(context, ServiceDatabaseSimpleNameProvider()),
            Constants.getDatabaseManagerProvider().buildServiceDatabaseManager()
        ).also {
            serviceDatabaseStorage = it
            DebugLogger.info(tag, "Create storage for service")
        }
    }

    @Synchronized
    fun getServiceBinaryDataHelper(context: Context): IBinaryDataHelper {
        return serviceBinaryDataHelperWrapper ?: BinaryDataHelperWrapper(
            context,
            StorageType.SERVICE,
            getRawServiceBinaryDataHelper(context)
        ).also {
            serviceBinaryDataHelperWrapper = it
            DebugLogger.info(tag, "Create binary data helper for service")
        }
    }

    @Synchronized
    fun getServiceBinaryDataHelperForMigration(context: Context): IBinaryDataHelper =
        getRawServiceBinaryDataHelper(context)

    private fun getRawServiceBinaryDataHelper(context: Context): IBinaryDataHelper {
        return serviceBinaryDataHelper ?: BinaryDataHelper(
            SimpleDBConnector(getStorageForService(context)),
            Constants.BinaryDataTable.TABLE_NAME
        ).also {
            serviceBinaryDataHelper = it
            DebugLogger.info(tag, "Create raw binary data helper for service")
        }
    }

    @Synchronized
    fun getServicePreferenceDbHelper(context: Context): IKeyValueTableDbHelper {
        return servicePreferenceDbHelperWrapper ?: KeyValueTableDbHelperWrapper(
            context,
            StorageType.SERVICE,
            getRawServicePreferenceDbHelper(context)
        ).also {
            servicePreferenceDbHelperWrapper = it
            DebugLogger.info(tag, "Create preference db helper for service")
        }
    }

    @Synchronized
    fun getServicePreferenceDbHelperForMigration(context: Context): IKeyValueTableDbHelper =
        getRawServicePreferenceDbHelper(context)

    private fun getRawServicePreferenceDbHelper(context: Context): IKeyValueTableDbHelper {
        return servicePreferenceDbHelper ?: KeyValueTableDbHelper(
            getStorageForService(context),
            Constants.PreferencesTable.TABLE_NAME
        ).also {
            servicePreferenceDbHelper = it
            DebugLogger.info(tag, "Create raw preference db helper for service")
        }
    }

    @Synchronized
    fun getServiceTempCacheStorage(context: Context): TempCacheStorage {
        return serviceTempCacheStorage ?: TempCacheDbHelperWrapper(
            context,
            StorageType.SERVICE,
            getRawServiceTempCacheStorage(context)
        ).also {
            serviceTempCacheStorage = it
            DebugLogger.info(tag, "Create temp cache storage for service")
        }
    }

    @Synchronized
    fun getServiceTempCacheStorageForMigration(context: Context): TempCacheStorage {
        return getRawServiceTempCacheStorage(context)
    }

    private fun getRawServiceTempCacheStorage(context: Context): TempCacheStorage {
        return rawServiceTempCacheStorage ?: TempCacheDbHelper(
            SimpleDBConnector(getStorageForService(context)),
            TempCacheTable.TABLE_NAME
        ).also {
            rawServiceTempCacheStorage = it
            DebugLogger.info(tag, "Create raw temp cache storage for service")
        }
    }

    @Synchronized
    fun createComponentLegacyStorageForMigration(
        context: Context,
        key: String,
        nameProvider: DatabaseSimpleNameProvider,
        tablesManager: TablesManager
    ): DatabaseStorage = DatabaseStorage(
        context,
        databaseStoragePathProviderFactory.create(key, false).getPath(context, nameProvider),
        tablesManager
    )

    @Synchronized
    fun getComponentStorage(
        context: Context,
        componentId: ComponentId
    ): DatabaseStorage {
        val databaseName = ComponentDatabaseSimpleNameProvider(componentId).databaseName
        return databaseStorages.getOrPut(databaseName) {
            DebugLogger.info(tag, "Create component storage for $componentId; database name: $databaseName")
            DatabaseStorage(
                context,
                databaseStoragePathProviderFactory.create(databaseName, false)
                    .getPath(context, ComponentDatabaseSimpleNameProvider(componentId)),
                Constants.getDatabaseManagerProvider().buildComponentDatabaseManager(componentId)
            )
        }
    }

    @Synchronized
    fun getComponentPreferenceDbHelper(
        context: Context,
        componentId: ComponentId
    ): IKeyValueTableDbHelper {
        val databaseName = ComponentDatabaseSimpleNameProvider(componentId).databaseName
        return databaseHelpers.getOrPut(databaseName) {
            DebugLogger.info(
                tag,
                "Create component preference db helper for $componentId; database name: $databaseName"
            )
            KeyValueTableDbHelper(
                getComponentStorage(context, componentId),
                Constants.PreferencesTable.TABLE_NAME
            )
        }
    }

    @Synchronized
    fun getComponentBinaryDataHelper(
        context: Context,
        componentId: ComponentId
    ): IBinaryDataHelper {
        val databaseName = ComponentDatabaseSimpleNameProvider(componentId).databaseName
        return componentBinaryDbHelpers.getOrPut(databaseName) {
            DebugLogger.info(
                tag,
                "Create component binary data helper for $componentId; database name: $databaseName"
            )
            BinaryDataHelper(
                SimpleDBConnector(getComponentStorage(context, componentId)),
                Constants.BinaryDataTable.TABLE_NAME
            )
        }
    }
}
