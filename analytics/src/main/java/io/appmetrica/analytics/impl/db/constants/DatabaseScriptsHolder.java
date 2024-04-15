package io.appmetrica.analytics.impl.db.constants;

import android.database.sqlite.SQLiteDatabase;
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServicesDatabase;
import io.appmetrica.analytics.modulesapi.internal.common.TableDescription;
import java.sql.SQLException;

public class DatabaseScriptsHolder {

    //
    // +------------------------------------------------------------+
    // | Creating/Dropping scripts for tables related to reporting  |
    // +------------------------------------------------------------+
    // ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||

    public static class ComponentDatabaseCreateScript extends DatabaseScript {

        @Override
        public void runScript(final SQLiteDatabase database) throws SQLException {
            // Table for reports
            database.execSQL(Constants.EventsTable.CREATE_TABLE);

            // Table for sessions
            database.execSQL(Constants.SessionTable.CREATE_TABLE);

            database.execSQL(Constants.PreferencesTable.CREATE_TABLE);

            database.execSQL(Constants.BinaryDataTable.CREATE_TABLE);
        }

    }

    public static class ComponentDatabaseDropScript extends DatabaseScript {

        @Override
        public void runScript(final SQLiteDatabase database) throws SQLException {
            // Table for reports
            database.execSQL(Constants.EventsTable.DROP_TABLE);

            // Table for sessions
            database.execSQL(Constants.SessionTable.DROP_TABLE);

            //Table for preferences
            database.execSQL(Constants.PreferencesTable.DROP_TABLE);

            database.execSQL(Constants.BinaryDataTable.DROP_TABLE);
        }

    }

    //
    // +------------------------------------------------------------+
    // | Creating/Dropping scripts for tables related to service    |
    // +------------------------------------------------------------+
    // ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||

    public static class ServiceDatabaseCreateScript extends DatabaseScript {
        private static final String TAG = "[DatabaseProviderCreateScript]";

        @Override
        public void runScript(final SQLiteDatabase database) throws SQLException {

            database.execSQL(Constants.PreferencesTable.CREATE_TABLE);

            database.execSQL(Constants.BinaryDataTable.CREATE_TABLE);

            database.execSQL(TempCacheTable.CREATE_TABLE);

            for (ModuleServicesDatabase moduleServicesDatabase :
                GlobalServiceLocator.getInstance().getModulesController().collectModuleServiceDatabases()) {
                for (TableDescription tableDescription : moduleServicesDatabase.getTables()) {
                    YLogger.info(TAG, "Exec SQL for module: %s", tableDescription.getCreateTableScript());
                    database.execSQL(tableDescription.getCreateTableScript());
                }
            }
        }
    }

    public static class ServiceDatabaseDropScript extends DatabaseScript {

        @Override
        public void runScript(final SQLiteDatabase database) throws SQLException {

            database.execSQL(Constants.PreferencesTable.DROP_TABLE);

            database.execSQL(Constants.BinaryDataTable.DROP_TABLE);

            database.execSQL(TempCacheTable.DROP_TABLE);

            for (ModuleServicesDatabase moduleServicesDatabase :
                GlobalServiceLocator.getInstance().getModulesController().collectModuleServiceDatabases()) {
                for (TableDescription tableDescription : moduleServicesDatabase.getTables()) {
                    database.execSQL(tableDescription.getDropTableScript());
                }
            }
        }
    }

    public static class DatabaseClientCreateScript extends DatabaseScript {

        @Override
        public void runScript(final SQLiteDatabase database) throws SQLException {
            database.execSQL(Constants.PreferencesTable.CREATE_TABLE);
        }

    }

    public static class DatabaseClientDropScript extends DatabaseScript {

        @Override
        public void runScript(final SQLiteDatabase database) throws SQLException {
            database.execSQL(Constants.PreferencesTable.DROP_TABLE);
        }
    }

    public static class DatabaseAutoInappCreateScript extends DatabaseScript {

        @Override
        public void runScript(final SQLiteDatabase database) throws SQLException {
            database.execSQL(Constants.BinaryDataTable.CREATE_TABLE);
        }

    }

    public static class DatabaseAutoInappDropScript extends DatabaseScript {

        @Override
        public void runScript(final SQLiteDatabase database) throws SQLException {
            database.execSQL(Constants.BinaryDataTable.DROP_TABLE);
        }
    }
}
