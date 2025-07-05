package io.appmetrica.analytics.coreapi.internal.io;

public interface IExecutionPolicy {

    String description();

    boolean canBeExecuted();
}
