package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.utils.PublicLogger;

abstract class BaseTrimmer<T> implements Trimmer<T> {

    private final int mMaxSize;
    private final String mLogName;
    @NonNull protected final PublicLogger mPublicLogger;

    public BaseTrimmer(int maxSize, @NonNull String logName, @NonNull PublicLogger logger) {
        mMaxSize = maxSize;
        mLogName = logName;
        mPublicLogger = logger;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public int getMaxSize() {
        return mMaxSize;
    }

    @NonNull
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public String getLogName() {
        return mLogName;
    }
}
