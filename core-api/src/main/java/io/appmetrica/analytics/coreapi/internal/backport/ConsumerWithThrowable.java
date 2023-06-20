package io.appmetrica.analytics.coreapi.internal.backport;

import androidx.annotation.NonNull;

public interface ConsumerWithThrowable<T> {

    void consume(@NonNull T input) throws Throwable;
}
