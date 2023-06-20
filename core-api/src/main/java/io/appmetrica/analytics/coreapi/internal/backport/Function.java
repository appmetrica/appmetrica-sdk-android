package io.appmetrica.analytics.coreapi.internal.backport;

public interface Function<T, R> {

    R apply(T input);

}
