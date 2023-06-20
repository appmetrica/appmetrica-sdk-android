package io.appmetrica.analytics.impl.id;

public interface RetryStrategy {

    boolean nextAttempt();

    void reset();

    int getTimeout();
}
