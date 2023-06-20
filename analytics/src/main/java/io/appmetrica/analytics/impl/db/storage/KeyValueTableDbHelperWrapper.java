package io.appmetrica.analytics.impl.db.storage;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.impl.db.StorageType;
import io.appmetrica.analytics.impl.utils.DebugAssert;
import java.util.Set;

class KeyValueTableDbHelperWrapper implements IKeyValueTableDbHelper {

    @NonNull
    private final Context context;
    @NonNull
    private final StorageType storageType;
    @NonNull
    private final IKeyValueTableDbHelper actualHelper;

    public KeyValueTableDbHelperWrapper(@NonNull Context context,
                                        @NonNull StorageType storageType,
                                        @NonNull IKeyValueTableDbHelper actualHelper) {
        this.context = context;
        this.storageType = storageType;
        this.actualHelper = actualHelper;
    }

    @Override
    public void commit() {
        actualHelper.commit();
    }

    @Nullable
    @Override
    public String getString(String key, String defValue) {
        checkMigrated();
        return actualHelper.getString(key, defValue);
    }

    @Override
    public int getInt(String key, int defValue) {
        checkMigrated();
        return actualHelper.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        checkMigrated();
        return actualHelper.getLong(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        checkMigrated();
        return actualHelper.getBoolean(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        checkMigrated();
        return actualHelper.getFloat(key, defValue);
    }

    @Override
    public IKeyValueTableDbHelper remove(String key) {
        checkMigrated();
        actualHelper.remove(key);
        return this;
    }

    @Override
    public IKeyValueTableDbHelper put(String key, String value) {
        checkMigrated();
        actualHelper.put(key, value);
        return this;
    }

    @Override
    public IKeyValueTableDbHelper put(String key, long value) {
        checkMigrated();
        actualHelper.put(key, value);
        return this;
    }

    @Override
    public IKeyValueTableDbHelper put(String key, int value) {
        checkMigrated();
        actualHelper.put(key, value);
        return this;
    }

    @Override
    public IKeyValueTableDbHelper put(String key, boolean value) {
        checkMigrated();
        actualHelper.put(key, value);
        return this;
    }

    @Override
    public IKeyValueTableDbHelper put(String key, float value) {
        checkMigrated();
        actualHelper.put(key, value);
        return this;
    }

    @Override
    public boolean containsKey(@NonNull String key) {
        return actualHelper.containsKey(key);
    }

    @NonNull
    @Override
    public Set<String> keys() {
        return actualHelper.keys();
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
    IKeyValueTableDbHelper getActualHelper() {
        return actualHelper;
    }
}
