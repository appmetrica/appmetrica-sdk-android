package io.appmetrica.analytics.impl;

import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;

public class ServiceMigrationManager extends MigrationManager {

    @NonNull
    private final VitalCommonDataProvider vitalCommonDataProvider;

    public ServiceMigrationManager() {
        this(
            GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProviderForMigration()
        );
    }

    @VisibleForTesting
    ServiceMigrationManager(
        @NonNull VitalCommonDataProvider vitalCommonDataProvider
    ) {
        this.vitalCommonDataProvider = vitalCommonDataProvider;
    }

    @Override
    SparseArray<MigrationScript> getScripts() {
        return new SparseArray<>();
    }

    @Override
    protected int getLastApiLevel() {
        return vitalCommonDataProvider.getLastMigrationApiLevel();
    }

    @Override
    protected void putLastApiLevel(final int apiLevel) {
        vitalCommonDataProvider.setLastMigrationApiLevel(apiLevel);
    }
}
