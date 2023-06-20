package io.appmetrica.analytics.coreapi.internal.backport;

public interface Consumer<T> {

    void consume(T input);

}
