package io.appmetrica.analytics.impl.db;

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.Collection;

public class TablesManager {

    public static class Creator {

        public TablesManager createTablesManager(@NonNull String logIdentifier,
                                                 @NonNull DatabaseScript createScript,
                                                 @NonNull DatabaseScript dropDcript,
                                                 @NonNull HashMultimap<Integer, DatabaseScript> upgradeScripts,
                                                 @NonNull TablesValidator tablesValidator) {
            return new TablesManager(
                    logIdentifier,
                    createScript,
                    dropDcript,
                    upgradeScripts,
                    tablesValidator
            );
        }
    }

    private static final String TAG = "[DbTablesManager]";

    private final String mDatabaseLogIdentifier;
    private final DatabaseScript mCreateScript;
    private final DatabaseScript mDropScript;
    @NonNull
    private final HashMultimap<Integer, DatabaseScript> mUpgradeScripts;
    private final TablesValidator mValidator;

    private TablesManager(
            String logIdentifier,
            DatabaseScript createScript,
            DatabaseScript dropScript,
            @NonNull HashMultimap<Integer, DatabaseScript> upgradeScripts,
            TablesValidator validator) {
        mDatabaseLogIdentifier = logIdentifier;
        mCreateScript = createScript;
        mDropScript = dropScript;
        mUpgradeScripts = upgradeScripts;
        mValidator = validator;
    }

    @VisibleForTesting
    TablesValidator getValidator() {
        return mValidator;
    }

    public void onOpen(SQLiteDatabase db) {
        try {
            YLogger.debug(TAG, "OnOpen was called for %s", mDatabaseLogIdentifier);
            if (mValidator != null) {
                if (mValidator.isDbSchemeValid(db) == false) {
                    recreateDatabase(db);
                }
            }
        } catch (Throwable exception) {
            YLogger.error(TAG, "Exception was occurred during opening %s.\n%s", mDatabaseLogIdentifier, exception);
        }
    }

    public void onCreate(final SQLiteDatabase database) {
        YLogger.debug(TAG, "OnCreate was called for %s", mDatabaseLogIdentifier);
        createDatabase(mCreateScript, database);
    }

    @VisibleForTesting
    void createDatabase(DatabaseScript createScript, SQLiteDatabase database) {
        try {
            createScript.runScript(database);
        } catch (Throwable exception) {
            YLogger.error(TAG, "Exception was occurred while creating %s.\n%s", mDatabaseLogIdentifier, exception);
        }
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

        boolean hasErrors = true;

        if (newVersion > oldVersion) {
            try {
                hasErrors = false;
                for (int version = oldVersion + 1; version <= newVersion; ++ version) {
                    Collection<DatabaseScript> versionScripts = mUpgradeScripts.get(version);
                    if (versionScripts != null) {
                        YLogger.info(
                            TAG, "Upgrading %s ... to v%d with %d scripts",
                            mDatabaseLogIdentifier, version, versionScripts.size()
                        );
                        for (DatabaseScript databaseScript : versionScripts) {
                            databaseScript.runScript(database);
                        }
                    }
                }
            } catch (Throwable exception) {
                YLogger.error(TAG, "Exception was occurred while upgrading %s.\n%s", mDatabaseLogIdentifier, exception);
                hasErrors = true;
            }
        }

        hasErrors |= mValidator.isDbSchemeValid(database) == false;

        if (hasErrors) {
            recreateDatabase(database);
        }
    }

    public void onDowngrade(@NonNull SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion > newVersion) {
            recreateDatabase(database);
        }
    }

    @VisibleForTesting
    void recreateDatabase(SQLiteDatabase database) {
        YLogger.debug(TAG, "recreate %s due to some errors.", mDatabaseLogIdentifier);
        dropDatabase(mDropScript, database);
        createDatabase(mCreateScript, database);
    }

    private void dropDatabase(DatabaseScript dropScript, SQLiteDatabase database) {
        try {
            dropScript.runScript(database);
        } catch (Throwable exception) {
            YLogger.error(TAG, "Exception was occurred while dropping %s.\n%s", mDatabaseLogIdentifier, exception);
        }
    }
}
