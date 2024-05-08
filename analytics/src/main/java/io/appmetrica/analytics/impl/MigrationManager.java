package io.appmetrica.analytics.impl;

import android.content.Context;
import android.util.SparseArray;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.logger.internal.YLogger;

public abstract class MigrationManager {

    private static final String TAG = "[MigrationManager]";

    public interface MigrationScript {
        public void run(Context context);
    }

    protected abstract SparseArray<MigrationScript> getScripts();

    public synchronized void checkMigration(final Context context) {
        final int apiLevelFrom = getLastApiLevel();
        final int apiLevelTo = getCurrentApiLevel();
        YLogger.debug(TAG, "Try to migrate from api level %d to %d", apiLevelFrom, apiLevelTo);
        if (apiLevelFrom != apiLevelTo) {
            if (apiLevelFrom < apiLevelTo) {
                YLogger.debug(TAG, "Need to migrate from api level %d to %d", apiLevelFrom, apiLevelTo);
                migrate(context, apiLevelFrom, apiLevelTo);
            }
            putLastApiLevel(apiLevelTo);
        }
    }

    public int getCurrentApiLevel() {
        return AppMetrica.getLibraryApiLevel();
    }

    protected abstract int getLastApiLevel();

    protected abstract void putLastApiLevel(final int apiLevel);

    private void migrate(final Context context, final int fromApiLevel, final int toApiLevel) {
        SparseArray<MigrationScript> migrationScripts = getScripts();
        for (int i = fromApiLevel + 1; i <= toApiLevel; i++) {
            MigrationScript script = migrationScripts.get(i);
            if (script != null) {
                YLogger.debug(TAG, "Run script to migrate to api level %d:%s", i, script.getClass().getName());
                script.run(context);
            }
        }
    }
}
