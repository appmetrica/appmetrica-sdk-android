package io.appmetrica.analytics.coreutils.internal.limitation;

import androidx.annotation.Nullable;

public interface Trimmer<T> {

    @Nullable
    T trim(@Nullable T data);

}
