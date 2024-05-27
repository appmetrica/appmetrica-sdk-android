package io.appmetrica.analytics.impl.db.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper;
import io.appmetrica.analytics.coreapi.internal.data.TempCacheStorage;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.db.DatabaseManagerProvider;
import io.appmetrica.analytics.impl.db.DatabaseStorage;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.StorageType;
import io.appmetrica.analytics.impl.db.connectors.LockedOnFileDBConnector;
import io.appmetrica.analytics.impl.db.connectors.SimpleDBConnector;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.db.constants.TempCacheTable;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static io.appmetrica.analytics.impl.db.constants.Constants.PreferencesTable;

public class DatabaseStorageFactory {

    private static final String TAG = "[DatabaseStorageFactory]";

    @SuppressLint("StaticFieldLeak")
    private volatile static DatabaseStorageFactory sStorageFactory;

    public static DatabaseStorageFactory getInstance(Context context) {
        if (sStorageFactory == null) {
            synchronized (DatabaseStorageFactory.class) {
                if (sStorageFactory == null) {
                    DebugLogger.INSTANCE.info(TAG, "Create default instance.");
                    sStorageFactory = new DatabaseStorageFactory(context.getApplicationContext(), null);
                }
            }
        }
        return sStorageFactory;
    }

    public static void initWithOverwrittenDbStorage(@NonNull Context context, @Nullable File file) {
        if (sStorageFactory == null) {
            synchronized (DatabaseStorageFactory.class) {
                if (sStorageFactory == null) {
                    DebugLogger.INSTANCE.info(TAG, "Create instance with overwrittenDbStoragePath");
                    sStorageFactory = new DatabaseStorageFactory(context.getApplicationContext(), file);
                }
            }
        }
    }

    private final Map<String, DatabaseStorage> databaseStorages = new HashMap<String, DatabaseStorage>();
    private final Map<String, IKeyValueTableDbHelper> mDbHelpers = new HashMap<String, IKeyValueTableDbHelper>();
    private final Map<String, IBinaryDataHelper> mComponentBinaryDbHelpers = new HashMap<String, IBinaryDataHelper>();

    @NonNull
    private final DatabaseManagerProvider databaseManagerProvider;
    @NonNull
    private final Context context;
    @Nullable
    private DatabaseStorage serviceDatabaseStorage;
    @Nullable
    private DatabaseStorage autoInappDatabaseStorage;
    @Nullable
    private IBinaryDataHelper serviceBinaryDataHelper;
    @Nullable
    private IBinaryDataHelper mServiceBinaryDataHelperWrapper;
    @Nullable
    private IBinaryDataHelper autoInappBinaryDataHelper;
    @Nullable
    private IBinaryDataHelper autoInappBinaryDataHelperWrapper;
    @Nullable
    private IKeyValueTableDbHelper mServicePreferencesDbHelper;
    @Nullable
    private IKeyValueTableDbHelper servicePreferencesDbHelperWrapper;
    @Nullable
    private TempCacheStorage serviceTempCacheDbHelper;
    @Nullable
    private TempCacheStorage serviceTempCacheDbHelperWrapper;
    @Nullable
    private IKeyValueTableDbHelper mClientDbHelper;
    @Nullable
    private IKeyValueTableDbHelper clientDbHelperWrapper;
    @Nullable
    private LockedOnFileDBConnector clientDbConnector;
    @NonNull
    private final DatabaseStoragePathProviderFactory databaseStoragePathProviderFactory;

    public DatabaseStorageFactory(@NonNull Context context,
                                  @Nullable File systemOverwrittenDbDir) {
        this.context = context;
        databaseManagerProvider = Constants.getDatabaseManagerProvider();
        this.databaseStoragePathProviderFactory = new DatabaseStoragePathProviderFactory(systemOverwrittenDbDir);
    }

    public synchronized DatabaseStorage getStorageForComponent(final ComponentId componentId) {
        ComponentDatabaseSimpleNameProvider componentDatabaseSimpleNameProvider =
            new ComponentDatabaseSimpleNameProvider(componentId);
        DatabaseStorage storage = databaseStorages.get(componentDatabaseSimpleNameProvider.getDatabaseName());

        if (null != storage) {
            DebugLogger.INSTANCE.info(
                TAG,
                "Database instance IN USE, we will just return for %s",
                componentId
            );
        } else {
            DebugLogger.INSTANCE.info(
                TAG,
                "Database instance IS NULL, we will create a new one for %s",
                componentId
            );
            storage = new DatabaseStorage(
                context,
                databaseStoragePathProviderFactory.create(componentDatabaseSimpleNameProvider.getDatabaseName(), false)
                    .getPath(context, componentDatabaseSimpleNameProvider),
                databaseManagerProvider.buildComponentDatabaseManager(componentId)
            );
            databaseStorages.put(componentDatabaseSimpleNameProvider.getDatabaseName(), storage);
        }

        return storage;
    }

    private DatabaseStorage getStorageForAutoInapp() {
        if (autoInappDatabaseStorage == null) {
            autoInappDatabaseStorage = new DatabaseStorage(
                context,
                databaseStoragePathProviderFactory.create("autoinapp", false)
                    .getPath(context, new AutoInappDatabaseSimpleNameProvider()),
                databaseManagerProvider.buildAutoInappDatabaseManager()
            );
        }
        return autoInappDatabaseStorage;
    }

    public synchronized DatabaseStorage getStorageForService() {
        if (serviceDatabaseStorage == null) {
            serviceDatabaseStorage = new DatabaseStorage(
                context,
                databaseStoragePathProviderFactory.create("service", true)
                    .getPath(context, new ServiceDatabaseSimpleNameProvider()),
                databaseManagerProvider.buildServiceDatabaseManager()
            );
        }
        return serviceDatabaseStorage;
    }

    public synchronized IKeyValueTableDbHelper getPreferencesDbHelper(ComponentId componentId) {
        String key = new ComponentDatabaseSimpleNameProvider(componentId).getDatabaseName();
        IKeyValueTableDbHelper dbHelper = mDbHelpers.get(key);
        if (dbHelper == null) {
            DatabaseStorage storage = getStorageForComponent(componentId);
            dbHelper = new KeyValueTableDbHelper(storage, PreferencesTable.TABLE_NAME);
            mDbHelpers.put(key, dbHelper);
        }
        return dbHelper;
    }

    @NonNull
    public synchronized IBinaryDataHelper getBinaryDbHelperForComponent(@NonNull ComponentId componentId) {
        String key = new ComponentDatabaseSimpleNameProvider(componentId).getDatabaseName();
        IBinaryDataHelper dbHelper = mComponentBinaryDbHelpers.get(key);
        if (dbHelper == null) {
            DatabaseStorage storage = getStorageForComponent(componentId);
            dbHelper = new BinaryDataHelper(
                new SimpleDBConnector(storage),
                Constants.BinaryDataTable.TABLE_NAME
            );
            mComponentBinaryDbHelpers.put(key, dbHelper);
        }
        return dbHelper;
    }

    public synchronized IBinaryDataHelper getServiceBinaryDataHelper() {
        if (mServiceBinaryDataHelperWrapper == null) {
            mServiceBinaryDataHelperWrapper = new BinaryDataHelperWrapper(
                context,
                StorageType.SERVICE,
                getRawServiceBinaryDataHelper()
            );
        }
        return mServiceBinaryDataHelperWrapper;
    }

    public synchronized IBinaryDataHelper getServiceBinaryDataHelperForMigration() {
        return getRawServiceBinaryDataHelper();
    }

    private IBinaryDataHelper getRawServiceBinaryDataHelper() {
        if (serviceBinaryDataHelper == null) {
            serviceBinaryDataHelper = new BinaryDataHelper(
                new SimpleDBConnector(getStorageForService()),
                Constants.BinaryDataTable.TABLE_NAME
            );
        }
        return serviceBinaryDataHelper;
    }

    public synchronized IBinaryDataHelper getAutoInappBinaryDataHelper() {
        if (autoInappBinaryDataHelperWrapper == null) {
            autoInappBinaryDataHelperWrapper = new BinaryDataHelperWrapper(
                context,
                StorageType.AUTO_INAPP,
                getRawAutoInappBinaryDataHelper()
            );
        }
        return autoInappBinaryDataHelperWrapper;
    }

    public synchronized IBinaryDataHelper getAutoInappBinaryDataHelperForMigration() {
        return getRawAutoInappBinaryDataHelper();
    }

    private IBinaryDataHelper getRawAutoInappBinaryDataHelper() {
        if (autoInappBinaryDataHelper == null) {
            autoInappBinaryDataHelper = new BinaryDataHelper(
                new SimpleDBConnector(getStorageForAutoInapp()),
                Constants.BinaryDataTable.TABLE_NAME
            );
        }
        return autoInappBinaryDataHelper;
    }

    public synchronized IKeyValueTableDbHelper getPreferencesDbHelperForService() {
        if (servicePreferencesDbHelperWrapper == null) {
            servicePreferencesDbHelperWrapper = new KeyValueTableDbHelperWrapper(
                context,
                StorageType.SERVICE,
                getRawPreferencesDbHelperForService()
            );
        }
        return servicePreferencesDbHelperWrapper;
    }

    public synchronized IKeyValueTableDbHelper getPreferencesDbHelperForServiceMigration() {
        return getRawPreferencesDbHelperForService();
    }

    private IKeyValueTableDbHelper getRawPreferencesDbHelperForService() {
        if (mServicePreferencesDbHelper == null) {
            mServicePreferencesDbHelper = new KeyValueTableDbHelper(
                getStorageForService(),
                PreferencesTable.TABLE_NAME
            );
        }
        return mServicePreferencesDbHelper;
    }

    public synchronized TempCacheStorage getTempCacheStorageForService() {
        if (serviceTempCacheDbHelperWrapper == null) {
            serviceTempCacheDbHelperWrapper = new TempCacheDbHelperWrapper(
                context,
                StorageType.SERVICE,
                getRawTempCacheDbHelperForService()
            );
        }
        return serviceTempCacheDbHelperWrapper;
    }

    public synchronized TempCacheStorage getServiceTempCacheDbHelperForMigration() {
        return getRawTempCacheDbHelperForService();
    }

    private TempCacheStorage getRawTempCacheDbHelperForService() {
        if (serviceTempCacheDbHelper == null) {
            serviceTempCacheDbHelper = new TempCacheDbHelper(
                new SimpleDBConnector(getStorageForService()),
                TempCacheTable.TABLE_NAME
            );
        }
        return serviceTempCacheDbHelper;
    }

    public synchronized IKeyValueTableDbHelper getClientDbHelper() {
        if (clientDbHelperWrapper == null) {
            clientDbHelperWrapper = new KeyValueTableDbHelperWrapper(
                context,
                StorageType.CLIENT,
                getRawClientDbHelper()
            );
        }
        return clientDbHelperWrapper;
    }

    public synchronized IKeyValueTableDbHelper getClientDbHelperForMigration() {
        return getRawClientDbHelper();
    }

    private IKeyValueTableDbHelper getRawClientDbHelper() {
        if (mClientDbHelper == null) {
            mClientDbHelper = new KeyValueTableDbHelper(Constants.PreferencesTable.TABLE_NAME, getClientDbConnector());
        }
        return mClientDbHelper;
    }

    @NonNull
    private synchronized LockedOnFileDBConnector getClientDbConnector() {
        if (clientDbConnector == null) {
            String dbPath = databaseStoragePathProviderFactory.create("client", true)
                .getPath(context, new ClientDatabaseSimpleNameProvider());
            clientDbConnector = new LockedOnFileDBConnector(
                context,
                dbPath,
                databaseManagerProvider.buildClientDatabaseManager()
            );
        }
        return clientDbConnector;
    }

    @VisibleForTesting
    public void setServicePreferencesHelperForMigration(IKeyValueTableDbHelper helper) {
        mServicePreferencesDbHelper = helper;
    }

    @VisibleForTesting
    public void setClientDbHelperForMigration(IKeyValueTableDbHelper helper) {
        mClientDbHelper = helper;
    }

    @VisibleForTesting
    public void setServicePreferencesHelper(@Nullable IKeyValueTableDbHelper helper) {
        servicePreferencesDbHelperWrapper = helper;
    }

    @VisibleForTesting
    public static void destroy() {
        sStorageFactory = null;
    }
}
