package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.HashMap;

class AppEnvironmentProvider {

    private final HashMap<String, AppEnvironment> mStorage = new HashMap<String, AppEnvironment>();

    public synchronized AppEnvironment getOrCreate(@NonNull ComponentId componentId,
                                                   @NonNull PublicLogger publicLogger,
                                                   @NonNull PreferencesComponentDbStorage preferences) {
        AppEnvironment appEnvironment = mStorage.get(componentId.toString());
        if (appEnvironment == null) {
            AppEnvironment.EnvironmentRevision revision = preferences.getAppEnvironmentRevision();
            YLogger.d("Create environment for component with id %s", componentId.toString());
            appEnvironment = new AppEnvironment(revision.value, revision.revisionNumber, publicLogger);
            mStorage.put(componentId.toString(), appEnvironment);
        }
        YLogger.d("will return environment %s", appEnvironment.toString());
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
