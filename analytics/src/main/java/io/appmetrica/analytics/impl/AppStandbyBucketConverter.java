package io.appmetrica.analytics.impl;

import android.annotation.TargetApi;
import android.app.usage.UsageStatsManager;
import android.os.Build;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;

public class AppStandbyBucketConverter {

    @Nullable
    public BackgroundRestrictionsState.AppStandByBucket fromIntToAppStandbyBucket(final int value) {
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.P)) {
            return fromIntToStandbyBucketP(value);
        } else {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    @Nullable
    private BackgroundRestrictionsState.AppStandByBucket fromIntToStandbyBucketP(final int value) {
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)) {
            if (value == UsageStatsManager.STANDBY_BUCKET_RESTRICTED) {
                return BackgroundRestrictionsState.AppStandByBucket.RESTRICTED;
            }
        }
        switch (value) {
            case UsageStatsManager.STANDBY_BUCKET_ACTIVE:
                return BackgroundRestrictionsState.AppStandByBucket.ACTIVE;
            case UsageStatsManager.STANDBY_BUCKET_FREQUENT:
                return BackgroundRestrictionsState.AppStandByBucket.FREQUENT;
            case UsageStatsManager.STANDBY_BUCKET_WORKING_SET:
                return BackgroundRestrictionsState.AppStandByBucket.WORKING_SET;
            case UsageStatsManager.STANDBY_BUCKET_RARE:
                return BackgroundRestrictionsState.AppStandByBucket.RARE;
            default:
                return null;
        }
    }

    @Nullable
    public String fromAppStandbyBucketToString(@Nullable BackgroundRestrictionsState.AppStandByBucket standByBucket) {
        if (standByBucket == null) {
            return null;
        }
        switch (standByBucket) {
            case ACTIVE:
                return "ACTIVE";
            case WORKING_SET:
                return "WORKING_SET";
            case FREQUENT:
                return "FREQUENT";
            case RARE:
                return "RARE";
            case RESTRICTED:
                return "RESTRICTED";
            default:
                return null;
        }
    }
}
