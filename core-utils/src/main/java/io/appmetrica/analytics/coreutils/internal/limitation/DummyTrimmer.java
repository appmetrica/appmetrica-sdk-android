package io.appmetrica.analytics.coreutils.internal.limitation;

import androidx.annotation.Nullable;

public final class DummyTrimmer<T> implements Trimmer<T> {

    @Nullable
    @Override
    public T trim(@Nullable T data) {
        return data;
    }
}
