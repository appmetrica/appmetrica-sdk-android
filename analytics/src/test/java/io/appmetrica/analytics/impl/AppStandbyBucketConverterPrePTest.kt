package io.appmetrica.analytics.impl

import android.app.usage.UsageStatsManager
import android.os.Build
import io.appmetrica.analytics.impl.BackgroundRestrictionsState.AppStandByBucket
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O])
@RunWith(ParameterizedRobolectricTestRunner::class)
internal class AppStandbyBucketConverterPrePTest(
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
            arrayOf(USAGE_STATE_MANAGER_APP_STAND_BY_BUCKET_EXEMPTED, null, null),
            arrayOf(UsageStatsManager.STANDBY_BUCKET_ACTIVE, null, null),
            arrayOf(UsageStatsManager.STANDBY_BUCKET_WORKING_SET, null, null),
            arrayOf(UsageStatsManager.STANDBY_BUCKET_FREQUENT, null, null),
            arrayOf(UsageStatsManager.STANDBY_BUCKET_RARE, null, null),
            arrayOf(UsageStatsManager.STANDBY_BUCKET_RESTRICTED, null, null),
            arrayOf(-1, null, null)
        )
    }
}
