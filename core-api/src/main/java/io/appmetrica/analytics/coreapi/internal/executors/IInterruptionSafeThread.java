package io.appmetrica.analytics.coreapi.internal.executors;

public interface IInterruptionSafeThread {

    boolean isRunning();

    void stopRunning();
}
