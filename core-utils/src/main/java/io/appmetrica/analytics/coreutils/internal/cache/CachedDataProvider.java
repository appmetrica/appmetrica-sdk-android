package io.appmetrica.analytics.coreutils.internal.cache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public interface CachedDataProvider {

    class CachedData<T> {

        private static final String TAG_PATTERN = "[CachedData-%s]";

        @NonNull
        private final String tag;
        private volatile long refreshTime;
        private volatile long expiryTime;
        private long mCachedTime = 0;
        @Nullable
        private T mCachedData = null;

        public CachedData(final long refreshTime, final long expiryTime, @NonNull String description) {
            this.tag = String.format(TAG_PATTERN, description);
            this.refreshTime = refreshTime;
            this.expiryTime = expiryTime;
        }

        @Nullable
        public T getData() {
            return mCachedData;
        }

        public void setData(@Nullable final T data) {
            mCachedData = data;
            updateCacheTime();
            DebugLogger.info(tag, "set data %s at %d", mCachedData, mCachedTime);
        }

        public final boolean isEmpty() {
            return (null == mCachedData);
        }

        private void updateCacheTime() {
            mCachedTime = System.currentTimeMillis();
        }

        public final boolean shouldUpdateData() {
            final long diffTime = System.currentTimeMillis() - mCachedTime;
            return diffTime > refreshTime || diffTime < 0;
        }

        public final boolean shouldClearData() {
            if (mCachedTime == 0) {
                return false;
            }
            final long diffTime = System.currentTimeMillis() - mCachedTime;
            return diffTime > expiryTime || diffTime < 0;
        }

        public void setExpirationPolicy(long refreshTime, long expiryTime) {
            this.refreshTime = refreshTime;
            this.expiryTime = expiryTime;
        }

        @VisibleForTesting
        public long getRefreshTime() {
            return refreshTime;
        }

        @VisibleForTesting
        public long getExpiryTime() {
            return expiryTime;
        }

        @NonNull
        @Override
        public String toString() {
            return "CachedData{" +
                "tag='" + tag + '\'' +
                ", refreshTime=" + refreshTime +
                ", expiryTime=" + expiryTime +
                ", mCachedTime=" + mCachedTime +
                ", mCachedData=" + mCachedData +
                '}';
        }
    }

}
