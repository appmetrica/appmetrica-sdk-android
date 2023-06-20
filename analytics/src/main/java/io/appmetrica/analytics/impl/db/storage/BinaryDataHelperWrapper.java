package io.appmetrica.analytics.impl.db.storage;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.impl.db.IBinaryDataHelper;
import io.appmetrica.analytics.impl.db.StorageType;
import io.appmetrica.analytics.impl.utils.DebugAssert;

class BinaryDataHelperWrapper implements IBinaryDataHelper {

    @NonNull
    private final Context context;
    @NonNull
    private final StorageType storageType;
    @NonNull
    private final IBinaryDataHelper actualHelper;

    BinaryDataHelperWrapper(@NonNull Context context,
                            @NonNull StorageType storageType,
                            @NonNull IBinaryDataHelper actualHelper) {
        this.context = context;
        this.storageType = storageType;
        this.actualHelper = actualHelper;
    }

    @Override
    public void insert(@NonNull String key, @NonNull byte[] value) {
        checkMigrated();
        actualHelper.insert(key, value);
    }

    @Override
    public byte[] get(@NonNull String key) {
        checkMigrated();
        return actualHelper.get(key);
    }

    @Override
    public void remove(@NonNull String key) {
        checkMigrated();
        actualHelper.remove(key);
    }

    private void checkMigrated() {
        if (BuildConfig.METRICA_DEBUG) {
            DebugAssert.assertMigrated(context, storageType);
        }
    }

    @VisibleForTesting
    StorageType getStorageType() {
        return storageType;
    }

    @VisibleForTesting
    IBinaryDataHelper getActualHelper() {
        return actualHelper;
    }
}
