package io.appmetrica.analytics.impl;

import androidx.annotation.Nullable;

public class BackgroundRestrictionsState {

    public enum AppStandByBucket {
        ACTIVE, WORKING_SET, FREQUENT, RARE, RESTRICTED
    }

    @Nullable
    public final AppStandByBucket mAppStandByBucket;
    @Nullable
    public final Boolean mBackgroundRestricted;

    public BackgroundRestrictionsState(@Nullable AppStandByBucket appStandByBucket,
                                       @Nullable Boolean backgroundRestricted) {
        mAppStandByBucket = appStandByBucket;
        mBackgroundRestricted = backgroundRestricted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BackgroundRestrictionsState that = (BackgroundRestrictionsState) o;

        if (mAppStandByBucket != that.mAppStandByBucket) return false;
        return mBackgroundRestricted != null ?
                mBackgroundRestricted.equals(that.mBackgroundRestricted) : that.mBackgroundRestricted == null;
    }

    @Override
    public int hashCode() {
        int result = mAppStandByBucket != null ? mAppStandByBucket.hashCode() : 0;
        result = 31 * result + (mBackgroundRestricted != null ? mBackgroundRestricted.hashCode() : 0);
        return result;
    }
}
