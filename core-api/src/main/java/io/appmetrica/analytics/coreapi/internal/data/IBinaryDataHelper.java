package io.appmetrica.analytics.coreapi.internal.data;

import androidx.annotation.NonNull;

public interface IBinaryDataHelper {

    void insert(@NonNull String key, @NonNull byte[] value);

    byte[] get(@NonNull String key);

    void remove(@NonNull String key);
}
