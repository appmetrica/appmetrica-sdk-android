package io.appmetrica.analytics.coreutils.internal.cache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.cache.CacheUpdateScheduler;
import io.appmetrica.analytics.coreapi.internal.cache.UpdateConditionsChecker;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public abstract class DataCache<T> implements UpdateConditionsChecker {

    private static final String TAG_PATTERN = "[DataCache-%s]";

    @NonNull
    private final String tag;
    @NonNull
    protected final CachedDataProvider.CachedData<T> mCachedData;
    @Nullable
    private CacheUpdateScheduler cacheUpdateScheduler;

    public DataCache(long refreshTime, long expiryTime, @NonNull String description) {
        tag = String.format(TAG_PATTERN, description);
        mCachedData = new CachedDataProvider.CachedData<T>(refreshTime, expiryTime, description);
    }

    public void updateData(@NonNull T newData) {
        if (shouldUpdate(newData)) {
            DebugLogger.info(tag, "Update cachedData with value %s", newData);
            mCachedData.setData(newData);
            if (cacheUpdateScheduler != null) {
                cacheUpdateScheduler.onStateUpdated();
            }
        }
    }

    @Nullable
    public T getData() {
        if (shouldUpdate() && cacheUpdateScheduler != null) {
            DebugLogger.info(
                    tag,
                    "Cache outdated, so update cached data: cached data = %s",
                    mCachedData
            );
            cacheUpdateScheduler.scheduleUpdateIfNeededNow();
        }
        if (mCachedData.shouldClearData()) {
            mCachedData.setData(null);
        }
        return mCachedData.getData();
    }

    public void setUpdateScheduler(@NonNull CacheUpdateScheduler cacheUpdateScheduler) {
        this.cacheUpdateScheduler = cacheUpdateScheduler;
    }

    public void updateCacheControl(long refreshTime, long expiryTime) {
        mCachedData.setExpirationPolicy(refreshTime, expiryTime);
    }

    @Override
    public boolean shouldUpdate() {
        return mCachedData.isEmpty() || mCachedData.shouldUpdateData();
    }

    protected abstract boolean shouldUpdate(@NonNull T newData);

    @VisibleForTesting
    @NonNull
    public CachedDataProvider.CachedData<T> getCachedData() {
        return mCachedData;
    }
}
