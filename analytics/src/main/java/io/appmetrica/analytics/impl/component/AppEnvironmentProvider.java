package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.HashMap;

class AppEnvironmentProvider {

    private static final String TAG = "[AppEnvironmentProvider]";

    private final HashMap<String, AppEnvironment> mStorage = new HashMap<String, AppEnvironment>();

    public synchronized AppEnvironment getOrCreate(@NonNull ComponentId componentId,
                                                   @NonNull PublicLogger publicLogger,
                                                   @NonNull PreferencesComponentDbStorage preferences) {
        AppEnvironment appEnvironment = mStorage.get(componentId.toString());
        if (appEnvironment == null) {
            AppEnvironment.EnvironmentRevision revision = preferences.getAppEnvironmentRevision();
            DebugLogger.info(TAG, "Create environment for component with id %s", componentId.toString());
            appEnvironment = new AppEnvironment(revision.value, revision.revisionNumber, publicLogger);
            mStorage.put(componentId.toString(), appEnvironment);
        }
        DebugLogger.info(TAG, "will return environment %s", appEnvironment.toString());
        return appEnvironment;
    }

    public synchronized boolean commitIfNeeded(AppEnvironment.EnvironmentRevision revision,
                                               PreferencesComponentDbStorage preferences) {
        if (revision.revisionNumber > preferences.getAppEnvironmentRevision().revisionNumber) {
            preferences.putAppEnvironmentRevision(revision).commit();
            return true;
        } else {
            return false;
        }
    }

    public synchronized void commit(AppEnvironment.EnvironmentRevision revision,
                                    PreferencesComponentDbStorage preferences) {
        preferences.putAppEnvironmentRevision(revision).commit();
    }
}
