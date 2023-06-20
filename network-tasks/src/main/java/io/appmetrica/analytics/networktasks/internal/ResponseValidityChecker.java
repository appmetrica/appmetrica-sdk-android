package io.appmetrica.analytics.networktasks.internal;

public interface ResponseValidityChecker {

    boolean isResponseValid(int responseCode);
}
