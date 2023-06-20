package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;

public class ClientTimeTracker {

    @Nullable
    private Long mCoreCreationRealtime;
    @NonNull
    private SystemTimeProvider mSystemTimeProvider;

    public ClientTimeTracker() {
        this(new SystemTimeProvider());
    }

    public void trackCoreCreation() {
        mCoreCreationRealtime = mSystemTimeProvider.elapsedRealtime();
    }

    @Nullable
    public Long getTimeSinceCoreCreation() {
        return mCoreCreationRealtime == null ? null : mSystemTimeProvider.elapsedRealtime() - mCoreCreationRealtime;
    }

    @VisibleForTesting
    ClientTimeTracker(@NonNull SystemTimeProvider systemTimeProvider) {
        mSystemTimeProvider = systemTimeProvider;
    }

    @VisibleForTesting
    @NonNull
    SystemTimeProvider getSystemTimeProvider() {
        return mSystemTimeProvider;
    }
}
