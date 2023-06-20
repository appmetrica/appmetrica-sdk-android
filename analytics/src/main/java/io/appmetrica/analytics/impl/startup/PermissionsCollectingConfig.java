package io.appmetrica.analytics.impl.startup;

public class PermissionsCollectingConfig {

    public final long mCheckIntervalSeconds;
    public final long mForceSendIntervalSeconds;

    public PermissionsCollectingConfig(final long checkIntervalSeconds, final long forceSendIntervalSeconds) {
        mCheckIntervalSeconds = checkIntervalSeconds;
        mForceSendIntervalSeconds = forceSendIntervalSeconds;
    }
}
