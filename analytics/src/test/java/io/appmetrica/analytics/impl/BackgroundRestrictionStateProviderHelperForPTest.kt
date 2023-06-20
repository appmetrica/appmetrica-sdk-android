package io.appmetrica.analytics.impl

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stubbing
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class BackgroundRestrictionStateProviderHelperForPTest : CommonTest() {

    private lateinit var context: Context

    @Mock
    private lateinit var converter: AppStandbyBucketConverter

    @Mock
    private lateinit var appStandByBucket: BackgroundRestrictionsState.AppStandByBucket

    @Mock
    private lateinit var activityStateManager: ActivityManager

    @Mock
    private lateinit var usageStatManager: UsageStatsManager
    private val appStandByState = UsageStatsManager.STANDBY_BUCKET_FREQUENT

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = TestUtils.createMockedContext()
        stubbing(converter) {
            on { fromIntToAppStandbyBucket(appStandByState) } doReturn appStandByBucket
        }
        stubbing(context) {
            on { getSystemService(Context.ACTIVITY_SERVICE) } doReturn activityStateManager
            on { getSystemService(Context.USAGE_STATS_SERVICE) } doReturn usageStatManager
        }
        stubbing(activityStateManager) {
            on { isBackgroundRestricted } doReturn true
        }
        stubbing(usageStatManager) {
            on { appStandbyBucket } doReturn appStandByState
        }
    }

    @Test
    fun bothServicesIsNull() {
        stubbing(context) {
            on { getSystemService(Context.ACTIVITY_SERVICE) } doReturn null
            on { getSystemService(Context.USAGE_STATS_SERVICE) } doReturn null
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .isEqualToComparingFieldByField(
                BackgroundRestrictionsState(null, null)
            )
    }

    @Test
    fun userStatManagerNull() {
        stubbing(context) {
            on { getSystemService(Context.USAGE_STATS_SERVICE) } doReturn null
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .isEqualToComparingFieldByField(BackgroundRestrictionsState(null, true))
    }

    @Test
    fun usageStatsManagerThrowsException() {
        stubbing(context) {
            on { getSystemService(Context.USAGE_STATS_SERVICE) } doThrow RuntimeException()
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .isEqualToComparingFieldByField(BackgroundRestrictionsState(null, true))
    }

    @Test
    fun ussageStatsManagerAccessThrowsException() {
        stubbing(usageStatManager) {
            on { appStandbyBucket } doThrow RuntimeException()
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .isEqualToComparingFieldByField(BackgroundRestrictionsState(null, true))
    }

    @Test
    fun activityManagerNull() {
        stubbing(context) {
            on { getSystemService(Context.ACTIVITY_SERVICE) } doReturn null
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .isEqualToComparingFieldByField(BackgroundRestrictionsState(appStandByBucket, null))
    }

    @Test
    fun activityManagerThrowsException() {
        stubbing(context) {
            on { getSystemService(Context.ACTIVITY_SERVICE) } doThrow RuntimeException()
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .isEqualToComparingFieldByField(BackgroundRestrictionsState(appStandByBucket, null))
    }

    @Test
    fun activityManagerAccessThrowsException() {
        stubbing(activityStateManager) {
            on { isBackgroundRestricted } doThrow RuntimeException()
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .isEqualToComparingFieldByField(BackgroundRestrictionsState(appStandByBucket, null))
    }

    @Test
    fun activityManagerReturnFalse() {
        stubbing(activityStateManager) {
            on { isBackgroundRestricted } doReturn false
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .isEqualToComparingFieldByField(BackgroundRestrictionsState(appStandByBucket, false))
    }
}
