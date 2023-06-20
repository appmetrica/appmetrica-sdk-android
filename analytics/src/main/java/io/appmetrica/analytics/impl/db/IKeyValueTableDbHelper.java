package io.appmetrica.analytics.impl.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Set;

public interface IKeyValueTableDbHelper {

    void commit();

    @Nullable
    String getString(final String key, final String defValue);

    int getInt(final String key, final int defValue);

    long getLong(final String key, final long defValue);

    boolean getBoolean(final String key, final boolean defValue);

    float getFloat(final String key, final float defValue);

    IKeyValueTableDbHelper remove(final String key);

    IKeyValueTableDbHelper put(final String key, final String value);

    IKeyValueTableDbHelper put(final String key, final long value);

    IKeyValueTableDbHelper put(final String key, final int value);

    IKeyValueTableDbHelper put(final String key, final boolean value);

    IKeyValueTableDbHelper put(final String key, final float value);

    boolean containsKey(@NonNull String key);

    @NonNull
    Set<String> keys();
}
