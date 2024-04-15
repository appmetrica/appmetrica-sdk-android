package io.appmetrica.analytics.impl.db;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.db.constants.TempCacheTable;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServicesDatabase;
import io.appmetrica.analytics.modulesapi.internal.common.TableDescription;
import java.util.HashMap;
import java.util.List;

public class DatabaseManagerProvider {

    private static final String DB_TAG_MAIN = "component-%s";
    private static final String DB_TAG_AUTO_INAPP = "auto_inapp";
    private static final String DB_TAG_SERVICE = "service database";

    @NonNull
    private final DatabaseScriptsProvider mDatabaseScriptsProvider;
    @NonNull
    private final DbTablesColumnsProvider mDbTablesColumnsProvider;
    @NonNull
    private final TablesManager.Creator mTablesManagerCreator;

    public DatabaseManagerProvider(@NonNull DatabaseScriptsProvider databaseScriptsProvider,
                                   @NonNull DbTablesColumnsProvider dbTablesColumnsProvider) {
        this(databaseScriptsProvider, dbTablesColumnsProvider, new TablesManager.Creator());
    }

    public DatabaseManagerProvider(@NonNull DatabaseScriptsProvider databaseScriptsProvider,
                                   @NonNull DbTablesColumnsProvider dbTablesColumnsProvider,
                                   @NonNull TablesManager.Creator tablesManagerCreator) {
        mDatabaseScriptsProvider = databaseScriptsProvider;
        mDbTablesColumnsProvider = dbTablesColumnsProvider;
        mTablesManagerCreator =tablesManagerCreator;
    }

    public TablesManager buildComponentDatabaseManager(ComponentId componentId) {
        return mTablesManagerCreator.createTablesManager(
                String.format(DB_TAG_MAIN, componentId.getApiKey() == null ? "main" : componentId.getApiKey()),
                mDatabaseScriptsProvider.getComponentDatabaseCreateScript(),
                mDatabaseScriptsProvider.getComponentDatabaseDropScript(),
                mDatabaseScriptsProvider.getComponentDatabaseUpgradeDbScripts(),
                new TablesValidatorImpl(DB_TAG_MAIN, mDbTablesColumnsProvider.getDbTablesColumns())
        );
    }

    public TablesManager buildAutoInappDatabaseManager() {
        HashMap<String, List<String>> tableColumnsToCheck = new HashMap<String, List<String>>();
        tableColumnsToCheck.put(Constants.BinaryDataTable.TABLE_NAME, Constants.BinaryDataTable.ACTUAL_COLUMNS);

        return mTablesManagerCreator.createTablesManager(
                DB_TAG_AUTO_INAPP,
                mDatabaseScriptsProvider.getDatabaseAutoInappCreateScript(),
                mDatabaseScriptsProvider.getDatabaseAutoInappDropScript(),
                new HashMultimap<Integer, DatabaseScript>(),
                new TablesValidatorImpl(DB_TAG_AUTO_INAPP, tableColumnsToCheck)
        );
    }

    public TablesManager buildServiceDatabaseManager() {
        HashMap<String, List<String>> tableColumnsToCheck = new HashMap<String, List<String>>();
        tableColumnsToCheck.put(Constants.PreferencesTable.TABLE_NAME, Constants.PreferencesTable.ACTUAL_COLUMNS);
        tableColumnsToCheck.put(Constants.BinaryDataTable.TABLE_NAME, Constants.BinaryDataTable.ACTUAL_COLUMNS);
        tableColumnsToCheck.put(TempCacheTable.TABLE_NAME, TempCacheTable.COLUMNS);

        for (ModuleServicesDatabase moduleServicesDatabase :
            GlobalServiceLocator.getInstance().getModulesController().collectModuleServiceDatabases()) {
            for (TableDescription tableDescription : moduleServicesDatabase.getTables()) {
                tableColumnsToCheck.put(tableDescription.getTableName(), tableDescription.getColumnNames());
            }
        }

        return mTablesManagerCreator.createTablesManager(
                DB_TAG_SERVICE,
                mDatabaseScriptsProvider.getDatabaseProviderCreateScript(),
                mDatabaseScriptsProvider.getDatabaseProviderDropScript(),
                mDatabaseScriptsProvider.getUpgradeServiceDbScripts(),
                new TablesValidatorImpl(DB_TAG_SERVICE, tableColumnsToCheck)
        );
    }

    public TablesManager buildClientDatabaseManager() {
        HashMap<String, List<String>> tableColumnsToCheck = new HashMap<String, List<String>>();
        tableColumnsToCheck.put(Constants.PreferencesTable.TABLE_NAME, Constants.PreferencesTable.ACTUAL_COLUMNS);

        return mTablesManagerCreator.createTablesManager(
                "client database",
                mDatabaseScriptsProvider.getDatabaseClientCreateScript(),
                mDatabaseScriptsProvider.getDatabaseClientDropScript(),
                mDatabaseScriptsProvider.getClientDatabaseUpgradeScripts(),
                new TablesValidatorImpl(DB_TAG_SERVICE, tableColumnsToCheck)
        );
    }
}
