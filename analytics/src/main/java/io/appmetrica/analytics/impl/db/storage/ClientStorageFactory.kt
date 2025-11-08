package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper
import io.appmetrica.analytics.impl.db.StorageType
import io.appmetrica.analytics.impl.db.connectors.LockedOnFileDBConnector
import io.appmetrica.analytics.impl.db.constants.Constants
import java.io.File

class ClientStorageFactory(outerStorageDirectory: File?) {
    private val databaseStoragePathProviderFactory = DatabaseStoragePathProviderFactory(outerStorageDirectory)
    private var clientDbHelper: IKeyValueTableDbHelper? = null
    private var clientDbHelperWrapper: IKeyValueTableDbHelper? = null
    private var clientDbConnector: LockedOnFileDBConnector? = null

    @Synchronized
    fun getClientDbHelper(context: Context): IKeyValueTableDbHelper {
        return clientDbHelperWrapper ?: KeyValueTableDbHelperWrapper(
            context,
            StorageType.CLIENT,
            getRawClientDbHelper(context)
        ).also { clientDbHelperWrapper = it }
    }

    @Synchronized
    fun getClientDbHelperForMigration(context: Context): IKeyValueTableDbHelper {
        return getRawClientDbHelper(context)
    }

    private fun getRawClientDbHelper(context: Context): IKeyValueTableDbHelper {
        return clientDbHelper
            ?: KeyValueTableDbHelper(Constants.PreferencesTable.TABLE_NAME, getClientDbConnector(context)).also {
                clientDbHelper = it
            }
    }

    @Synchronized
    private fun getClientDbConnector(context: Context): LockedOnFileDBConnector {
        return clientDbConnector ?: LockedOnFileDBConnector(
            context,
            context.getDbPath(),
            Constants.getDatabaseManagerProvider().buildClientDatabaseManager()
        ).also { clientDbConnector = it }
    }

    private fun Context.getDbPath(): String = databaseStoragePathProviderFactory.create("client", true)
        .getPath(this, ClientDatabaseSimpleNameProvider())
}
