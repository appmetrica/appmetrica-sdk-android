package io.appmetrica.analytics.impl

import android.annotation.TargetApi
import android.app.usage.UsageStatsManager
import android.os.Build
import io.appmetrica.analytics.impl.BackgroundRestrictionsState.AppStandByBucket
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(ParameterizedRobolectricTestRunner::class)
class AppStandbyBucketConverterPTest(
    input: Int,
    expected: AppStandByBucket?,
    expectedString: String?
) : AppStandbyBucketConverterBaseTest(
    input,
    expected,
    expectedString
) {

    companion object {
        @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
        @JvmStatic
        fun data(): List<Array<Any?>> = listOf(
            arrayOf(USAGE_STATE_MANAGER_APP_STAND_BY_BUCKET_EXEMPTED, AppStandByBucket.EXEMPTED, BUCKET_EXEMPTED),
            arrayOf(UsageStatsManager.STANDBY_BUCKET_ACTIVE, AppStandByBucket.ACTIVE, BUCKET_ACTIVE),
            arrayOf(UsageStatsManager.STANDBY_BUCKET_FREQUENT, AppStandByBucket.FREQUENT, BUCKET_FREQUENT),
            arrayOf(UsageStatsManager.STANDBY_BUCKET_WORKING_SET, AppStandByBucket.WORKING_SET, BUCKET_WORKING_SET),
            arrayOf(UsageStatsManager.STANDBY_BUCKET_RARE, AppStandByBucket.RARE, BUCKET_RARE),
            arrayOf(UsageStatsManager.STANDBY_BUCKET_RESTRICTED, AppStandByBucket.UNKNOWN, BUCKET_UNKNOWN),
            arrayOf(-1, AppStandByBucket.UNKNOWN, BUCKET_UNKNOWN)
        )
    }
}
