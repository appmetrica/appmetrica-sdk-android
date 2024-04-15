package io.appmetrica.analytics.impl.db;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.SdkData;
import io.appmetrica.analytics.impl.db.constants.DatabaseScriptsHolder;
import io.appmetrica.analytics.impl.db.constants.migrations.ClientDatabaseUpgradeScriptToV112;
import io.appmetrica.analytics.impl.db.constants.migrations.ComponentDatabaseUpgradeScriptToV112;
import io.appmetrica.analytics.impl.db.constants.migrations.ServiceDatabaseUpgradeScriptToV114;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServicesDatabase;
import io.appmetrica.analytics.modulesapi.internal.common.TableDescription;
import java.util.Map;

public class DatabaseScriptsProvider {

    private static final String TAG = "[DatabaseScriptsProvider]";
    @NonNull
    private final DatabaseScript componentDatabaseCreateScript;
    @NonNull
    private final DatabaseScript componentDatabaseDropScript;
    @NonNull
    private final DatabaseScript clientDatabaseCreateScript;
    @NonNull
    private final DatabaseScript clientDatabaseDropScript;
    @NonNull
    private final DatabaseScript serviceDatabaseCreateScript;
    @NonNull
    private final DatabaseScript serviceDatabaseDropScript;
    @NonNull
    private final DatabaseScript databaseAutoInappCreateScript;
    @NonNull
    private final DatabaseScript databaseAutoInappDropScript;

    @SuppressWarnings("checkstyle:methodlength")
    public DatabaseScriptsProvider() {
        componentDatabaseCreateScript = new DatabaseScriptsHolder.ComponentDatabaseCreateScript();
        componentDatabaseDropScript = new DatabaseScriptsHolder.ComponentDatabaseDropScript();
        clientDatabaseCreateScript = new DatabaseScriptsHolder.DatabaseClientCreateScript();
        clientDatabaseDropScript = new DatabaseScriptsHolder.DatabaseClientDropScript();
        serviceDatabaseCreateScript = new DatabaseScriptsHolder.ServiceDatabaseCreateScript();
        serviceDatabaseDropScript = new DatabaseScriptsHolder.ServiceDatabaseDropScript();
        databaseAutoInappCreateScript = new DatabaseScriptsHolder.DatabaseAutoInappCreateScript();
        databaseAutoInappDropScript = new DatabaseScriptsHolder.DatabaseAutoInappDropScript();
    }

    @NonNull
    private HashMultimap<Integer, DatabaseScript> collectUpgradeServiceDbScripts() {
        HashMultimap<Integer, DatabaseScript> result = new HashMultimap<>();
        result.put(SdkData.TEMP_CACHE_ADDED, new ServiceDatabaseUpgradeScriptToV114());

        for (ModuleServicesDatabase moduleServicesDatabase :
            GlobalServiceLocator.getInstance().getModulesController().collectModuleServiceDatabases()) {
            for (TableDescription tableDescription : moduleServicesDatabase.getTables()) {
                for (Map.Entry<Integer, DatabaseScript> scriptEntry :
                    tableDescription.getDatabaseProviderUpgradeScript().entrySet()) {
                    YLogger.info(
                        TAG,
                        "[%s] Add module service database provider upgrade scripts... to version: %d...",
                        tableDescription.getTableName(), scriptEntry.getKey()
                        );
                    result.put(scriptEntry.getKey(), scriptEntry.getValue());
                }
            }
        }

        return result;
    }

    @NonNull
    public HashMultimap<Integer, DatabaseScript> getComponentDatabaseUpgradeDbScripts() {
        HashMultimap<Integer, DatabaseScript> componentDatabaseUpgradeScripts =
            new HashMultimap<>();
        componentDatabaseUpgradeScripts.put(SdkData.INITIAL_API_LEVEL, new ComponentDatabaseUpgradeScriptToV112());
        return componentDatabaseUpgradeScripts;
    }

    @NonNull
    public HashMultimap<Integer, DatabaseScript> getUpgradeServiceDbScripts() {
        return collectUpgradeServiceDbScripts();
    }

    @NonNull
    public HashMultimap<Integer, DatabaseScript> getClientDatabaseUpgradeScripts() {
        HashMultimap<Integer, DatabaseScript> clientDatabaseUpgradeScripts =
            new HashMultimap<>();
        clientDatabaseUpgradeScripts.put(SdkData.INITIAL_API_LEVEL, new ClientDatabaseUpgradeScriptToV112());
        return clientDatabaseUpgradeScripts;
    }

    @NonNull
    public DatabaseScript getComponentDatabaseCreateScript() {
        return componentDatabaseCreateScript;
    }

    @NonNull
    public DatabaseScript getComponentDatabaseDropScript() {
        return componentDatabaseDropScript;
    }

    @NonNull
    public DatabaseScript getDatabaseClientCreateScript() {
        return clientDatabaseCreateScript;
    }

    @NonNull
    public DatabaseScript getDatabaseClientDropScript() {
        return clientDatabaseDropScript;
    }

    @NonNull
    public DatabaseScript getDatabaseAutoInappCreateScript() {
        return databaseAutoInappCreateScript;
    }

    @NonNull
    public DatabaseScript getDatabaseAutoInappDropScript() {
        return databaseAutoInappDropScript;
    }

    @NonNull
    public DatabaseScript getDatabaseProviderCreateScript() {
        return serviceDatabaseCreateScript;
    }

    @NonNull
    public DatabaseScript getDatabaseProviderDropScript() {
        return serviceDatabaseDropScript;
    }
}
