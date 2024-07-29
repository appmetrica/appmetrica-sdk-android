package io.appmetrica.analytics.impl.crash.jvm.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Anr {

    @NonNull
    public final AllThreads mAllThreads;
    @Nullable
    public final String mBuildId;
    @Nullable
    public final Boolean mIsOffline;

    public Anr(@NonNull AllThreads allThreads, @Nullable String buildId, @Nullable Boolean isOffline) {
        mAllThreads = allThreads;
        mBuildId = buildId;
        mIsOffline = isOffline;
    }
}
