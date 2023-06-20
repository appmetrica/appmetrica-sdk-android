package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;

public interface NonNullConsumer<T> {

    void consume(@NonNull T data);
}
