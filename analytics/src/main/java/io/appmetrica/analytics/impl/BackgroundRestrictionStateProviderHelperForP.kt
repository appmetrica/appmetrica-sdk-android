package io.appmetrica.analytics.impl

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.impl.BackgroundRestrictionsState.AppStandByBucket

@DoNotInline
@TargetApi(Build.VERSION_CODES.P)
internal object BackgroundRestrictionStateProviderHelperForP {
    @JvmStatic
    fun readBackgroundRestrictionsState(
        context: Context,
        converter: AppStandbyBucketConverter
    ): BackgroundRestrictionsState {
        return BackgroundRestrictionsState(
            SystemServiceUtils.accessSystemServiceByNameSafely<UsageStatsManager, AppStandByBucket?>(
                context,
                Context.USAGE_STATS_SERVICE,
                "getting app standby bucket",
                "usageStatsManager"
            ) { converter.fromIntToAppStandbyBucket(it.appStandbyBucket) },
            SystemServiceUtils.accessSystemServiceByNameSafely<ActivityManager, Boolean?>(
                context,
                Context.ACTIVITY_SERVICE,
                "getting is background restricted",
                "activityManager"
            ) { it.isBackgroundRestricted }
        )
    }
}
