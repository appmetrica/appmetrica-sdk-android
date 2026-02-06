package io.appmetrica.analytics.impl

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing

internal class BackgroundRestrictionStateProviderHelperForPTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    private val appStandByBucket: BackgroundRestrictionsState.AppStandByBucket = mock()
    private val appStandByState = UsageStatsManager.STANDBY_BUCKET_FREQUENT
    private val converter: AppStandbyBucketConverter = mock {
        on { fromIntToAppStandbyBucket(appStandByState) } doReturn appStandByBucket
    }
    private val activityStateManager: ActivityManager = mock {
        on { isBackgroundRestricted } doReturn true
    }

    private val usageStatManager: UsageStatsManager = mock {
        on { appStandbyBucket } doReturn appStandByState
    }

    @get:Rule
    val androidUtilsRule = staticRule<AndroidUtils> {
        on { AndroidUtils.isApiAchieved(Build.VERSION_CODES.P) } doReturn true
    }

    @Before
    fun setUp() {
        stubbing(context) {
            on { getSystemService(Context.ACTIVITY_SERVICE) } doReturn activityStateManager
            on { getSystemService(Context.USAGE_STATS_SERVICE) } doReturn usageStatManager
        }
    }

    @Test
    fun bothServicesIsNull() {
        stubbing(context) {
            on { getSystemService(Context.ACTIVITY_SERVICE) } doReturn null
            on { getSystemService(Context.USAGE_STATS_SERVICE) } doReturn null
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .usingRecursiveComparison()
            .isEqualTo(BackgroundRestrictionsState(null, null))
    }

    @Test
    fun userStatManagerNull() {
        stubbing(context) {
            on { getSystemService(Context.USAGE_STATS_SERVICE) } doReturn null
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .usingRecursiveComparison()
            .isEqualTo(BackgroundRestrictionsState(null, true))
    }

    @Test
    fun usageStatsManagerThrowsException() {
        stubbing(context) {
            on { getSystemService(Context.USAGE_STATS_SERVICE) } doThrow RuntimeException()
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .usingRecursiveComparison()
            .isEqualTo(BackgroundRestrictionsState(null, true))
    }

    @Test
    fun ussageStatsManagerAccessThrowsException() {
        stubbing(usageStatManager) {
            on { appStandbyBucket } doThrow RuntimeException()
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .usingRecursiveComparison()
            .isEqualTo(BackgroundRestrictionsState(null, true))
    }

    @Test
    fun activityManagerNull() {
        stubbing(context) {
            on { getSystemService(Context.ACTIVITY_SERVICE) } doReturn null
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .usingRecursiveComparison()
            .isEqualTo(BackgroundRestrictionsState(appStandByBucket, null))
    }

    @Test
    fun activityManagerThrowsException() {
        stubbing(context) {
            on { getSystemService(Context.ACTIVITY_SERVICE) } doThrow RuntimeException()
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .usingRecursiveComparison()
            .isEqualTo(BackgroundRestrictionsState(appStandByBucket, null))
    }

    @Test
    fun activityManagerAccessThrowsException() {
        stubbing(activityStateManager) {
            on { isBackgroundRestricted } doThrow RuntimeException()
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .usingRecursiveComparison()
            .isEqualTo(BackgroundRestrictionsState(appStandByBucket, null))
    }

    @Test
    fun activityManagerReturnFalse() {
        stubbing(activityStateManager) {
            on { isBackgroundRestricted } doReturn false
        }
        assertThat(BackgroundRestrictionStateProviderHelperForP.readBackgroundRestrictionsState(context, converter))
            .usingRecursiveComparison()
            .isEqualTo(BackgroundRestrictionsState(appStandByBucket, false))
    }
}
