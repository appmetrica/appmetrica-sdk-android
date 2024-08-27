package io.appmetrica.analytics.impl.service.migration;

import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.MigrationManager;
import io.appmetrica.analytics.impl.SdkData;
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
    protected SparseArray<MigrationScript> getScripts() {
        SparseArray<MigrationScript> migrations = new SparseArray<>(1);
        migrations.put(SdkData.INITIAL_API_LEVEL, new ServiceMigrationScriptToV112(vitalCommonDataProvider));
        migrations.put(SdkData.MODULE_CONFIGS_ADDED, new ServiceMigrationScriptToV115());
        return migrations;
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
