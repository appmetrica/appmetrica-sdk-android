package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.db.StorageType;

public class DebugAssert {

    public static void assertMigrated(@NonNull Context context, @NonNull StorageType storageType) {
        if (BuildConfig.METRICA_DEBUG) {
            switch (storageType) {
                case CLIENT:
                    assertClientMigrationChecked(context);
                    break;
                case SERVICE:
                    assertServiceMigrationChecked();
                    break;
                case AUTO_INAPP:
                    assertAutoInappMigrationChecked();
                    break;
                default:
                    break;
            }
        }
    }

    private static void assertServiceMigrationChecked() {
        final int lastMigrationVersion = GlobalServiceLocator.getInstance().getVitalDataProviderStorage()
            .getCommonDataProviderForMigration().getLastMigrationApiLevel();
        if (lastMigrationVersion != AppMetrica.getLibraryApiLevel()) {
            throw new AssertionError("Service migration is not checked");
        }
    }

    private static void assertClientMigrationChecked(@NonNull Context context) {
        final long lastMigrationVersion = ClientServiceLocator.getInstance()
            .getClientMigrationApiLevel(context);
        if (lastMigrationVersion != AppMetrica.getLibraryApiLevel()) {
            throw new AssertionError("Client migration is not checked");
        }
    }

    private static void assertAutoInappMigrationChecked() {
        // do nothing for now
    }
}
