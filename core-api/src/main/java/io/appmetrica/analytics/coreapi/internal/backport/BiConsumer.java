package io.appmetrica.analytics.coreapi.internal.backport;

public interface BiConsumer<T1, T2> {

    void consume(T1 firstArg, T2 secondArg);

}
