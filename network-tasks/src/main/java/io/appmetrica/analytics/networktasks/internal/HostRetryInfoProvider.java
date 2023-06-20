package io.appmetrica.analytics.networktasks.internal;

public interface HostRetryInfoProvider {

    int getNextSendAttemptNumber();

    long getLastAttemptTimeSeconds();

    void saveNextSendAttemptNumber(int nextSendAttemptNumber);

    void saveLastAttemptTimeSeconds(long lastAttemptTimeSeconds);
}
