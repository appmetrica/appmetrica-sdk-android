package io.appmetrica.analytics.impl.startup;

public interface StartupIdentifiersProvider {
    String getDeviceId();

    String getUuid();
}
