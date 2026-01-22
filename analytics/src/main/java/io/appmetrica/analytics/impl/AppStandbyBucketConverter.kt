package io.appmetrica.analytics.impl

import android.annotation.TargetApi
import android.app.usage.UsageStatsManager
import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils.isApiAchieved
import io.appmetrica.analytics.impl.BackgroundRestrictionsState.AppStandByBucket
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class AppStandbyBucketConverter {

    private val tag = "[AppStandbyBucketConverter]"

    private val usageStateManagerAppStandByBucketExempted = 5

    fun fromIntToAppStandbyBucket(value: Int): AppStandByBucket? {
        return if (isApiAchieved(Build.VERSION_CODES.P)) {
            value.fromIntToStandbyBucketP()
        } else {
            null
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun Int.fromIntToStandbyBucketP(): AppStandByBucket? {
        DebugLogger.info(tag, "fromIntToStandbyBucketP: %s", this)

        if (isApiAchieved(Build.VERSION_CODES.R)) {
            if (this == UsageStatsManager.STANDBY_BUCKET_RESTRICTED) {
                return AppStandByBucket.RESTRICTED
            }
        }
        return when (this) {
            usageStateManagerAppStandByBucketExempted -> AppStandByBucket.EXEMPTED
            UsageStatsManager.STANDBY_BUCKET_ACTIVE -> AppStandByBucket.ACTIVE
            UsageStatsManager.STANDBY_BUCKET_FREQUENT -> AppStandByBucket.FREQUENT
            UsageStatsManager.STANDBY_BUCKET_WORKING_SET -> AppStandByBucket.WORKING_SET
            UsageStatsManager.STANDBY_BUCKET_RARE -> AppStandByBucket.RARE
            else -> null
        }
    }

    fun fromAppStandbyBucketToString(standByBucket: AppStandByBucket?): String? {
        if (standByBucket == null) {
            return null
        }
        return when (standByBucket) {
            AppStandByBucket.EXEMPTED -> "EXEMPTED"
            AppStandByBucket.ACTIVE -> "ACTIVE"
            AppStandByBucket.WORKING_SET -> "WORKING_SET"
            AppStandByBucket.FREQUENT -> "FREQUENT"
            AppStandByBucket.RARE -> "RARE"
            AppStandByBucket.RESTRICTED -> "RESTRICTED"
            else -> null
        }
    }
}
