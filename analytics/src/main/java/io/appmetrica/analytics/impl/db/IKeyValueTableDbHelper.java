package io.appmetrica.analytics.impl.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Set;

public interface IKeyValueTableDbHelper {

    @Nullable
    String getString(@NonNull final String key, @Nullable final String defValue);

    int getInt(@NonNull final String key, final int defValue);

    long getLong(@NonNull final String key, final long defValue);

    boolean getBoolean(@NonNull final String key, final boolean defValue);

    float getFloat(@NonNull final String key, final float defValue);

    @NonNull
    IKeyValueTableDbHelper remove(@NonNull final String key);

    @NonNull
    IKeyValueTableDbHelper put(@NonNull final String key, @Nullable final String value);

    @NonNull
    IKeyValueTableDbHelper put(@NonNull final String key, final long value);

    @NonNull
    IKeyValueTableDbHelper put(@NonNull final String key, final int value);

    @NonNull
    IKeyValueTableDbHelper put(@NonNull final String key, final boolean value);

    @NonNull
    IKeyValueTableDbHelper put(@NonNull final String key, final float value);

    boolean containsKey(@NonNull String key);

    @NonNull
    Set<String> keys();

    void flush();

    void flushAsync();
}
