package io.appmetrica.analytics.impl.permissions

import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PermissionsCheckerTest : CommonTest() {

    @get:Rule
    val androidUtilsMockedStaticRule = staticRule<AndroidUtils>()

    @Test
    fun testStaticRetriever() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.JELLY_BEAN)).thenReturn(false)
        assertThat(PermissionsChecker().createPermissionsRetriever(RuntimeEnvironment.getApplication()))
            .isInstanceOf(StaticPermissionRetriever::class.java)
    }

    @Test
    fun testRuntimeRetriever() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.JELLY_BEAN)).thenReturn(true)
        assertThat(PermissionsChecker().createPermissionsRetriever(RuntimeEnvironment.getApplication()))
            .isInstanceOf(RuntimePermissionsRetriever::class.java)
    }
}
