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

    @Nullable
    @Override
    public String getString(@NonNull String key, @Nullable String defValue) {
        checkMigrated();
        return actualHelper.getString(key, defValue);
    }

    @Override
    public int getInt(@NonNull String key, int defValue) {
        checkMigrated();
        return actualHelper.getInt(key, defValue);
    }

    @Override
    public long getLong(@NonNull String key, long defValue) {
        checkMigrated();
        return actualHelper.getLong(key, defValue);
    }

    @Override
    public boolean getBoolean(@NonNull String key, boolean defValue) {
        checkMigrated();
        return actualHelper.getBoolean(key, defValue);
    }

    @Override
    public float getFloat(@NonNull String key, float defValue) {
        checkMigrated();
        return actualHelper.getFloat(key, defValue);
    }

    @NonNull
    @Override
    public IKeyValueTableDbHelper remove(@NonNull String key) {
        checkMigrated();
        actualHelper.remove(key);
        return this;
    }

    @NonNull
    @Override
    public IKeyValueTableDbHelper put(@NonNull String key, @Nullable String value) {
        checkMigrated();
        actualHelper.put(key, value);
        return this;
    }

    @NonNull
    @Override
    public IKeyValueTableDbHelper put(@NonNull String key, long value) {
        checkMigrated();
        actualHelper.put(key, value);
        return this;
    }

    @NonNull
    @Override
    public IKeyValueTableDbHelper put(@NonNull String key, int value) {
        checkMigrated();
        actualHelper.put(key, value);
        return this;
    }

    @NonNull
    @Override
    public IKeyValueTableDbHelper put(@NonNull String key, boolean value) {
        checkMigrated();
        actualHelper.put(key, value);
        return this;
    }

    @NonNull
    @Override
    public IKeyValueTableDbHelper put(@NonNull String key, float value) {
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

    @Override
    public void flush() {
        actualHelper.flush();
    }

    @Override
    public void flushAsync() {
        actualHelper.flushAsync();
    }

    private void checkMigrated() {
        if (BuildConfig.METRICA_DEBUG) {
            DebugAssert.assertMigrated(context, storageType);
        }
    }

    @VisibleForTesting
    @NonNull
    StorageType getStorageType() {
        return storageType;
    }

    @VisibleForTesting
    @NonNull
    IKeyValueTableDbHelper getActualHelper() {
        return actualHelper;
    }
}
