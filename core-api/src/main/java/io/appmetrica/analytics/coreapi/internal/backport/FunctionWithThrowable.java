package io.appmetrica.analytics.coreapi.internal.backport;

import androidx.annotation.NonNull;

public interface FunctionWithThrowable<T, R> {

    R apply(@NonNull T input) throws Throwable;
}
