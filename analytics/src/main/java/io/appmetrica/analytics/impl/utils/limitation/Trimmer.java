package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.Nullable;

public interface Trimmer<T> {

    @Nullable
    T trim(@Nullable T data);

}
