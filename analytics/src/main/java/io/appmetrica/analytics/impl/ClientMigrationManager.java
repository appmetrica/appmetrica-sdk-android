package io.appmetrica.analytics.impl;

import android.content.Context;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;

public class ClientMigrationManager extends MigrationManager {

    private final PreferencesClientDbStorage mClientStorage;

    public ClientMigrationManager(@NonNull Context context) {
        this(new PreferencesClientDbStorage(
                ClientServiceLocator.getInstance().getStorageFactory(context).getClientDbHelperForMigration(context)
        ));
    }

    @VisibleForTesting
    ClientMigrationManager(final PreferencesClientDbStorage clientPreferences) {
        mClientStorage = clientPreferences;
    }

    @Override
    protected SparseArray<MigrationScript> getScripts() {
        return new SparseArray<>();
    }

    @Override
    protected int getLastApiLevel() {
        return (int) mClientStorage.getClientApiLevel(-1);
    }

    @Override
    protected void putLastApiLevel(final int apiLevel) {
        mClientStorage.putClientApiLevel(apiLevel);
    }
}
