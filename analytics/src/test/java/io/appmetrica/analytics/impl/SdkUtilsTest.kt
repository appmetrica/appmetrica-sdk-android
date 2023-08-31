package io.appmetrica.analytics.impl

import android.util.Log
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.TestUtils
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SdkUtilsTest : CommonTest() {

    @get:Rule
    val logMockedStaticRule = staticRule<Log>()

    @Test
    fun onlyMetricaCrash() {
        val throwable = TestUtils.createThrowableMock(
            "at com.android.blabla\nat io.appmetrica.analytics.BlaBla\nat com.android.blabla"
        )
        assertThat(SdkUtils.isExceptionFromMetrica(throwable)).isTrue()
        assertThat(SdkUtils.isExceptionFromPushSdk(throwable)).isFalse()
    }

    @Test
    fun onlyPushCrash() {
        val throwable = TestUtils.createThrowableMock(
            "at com.android.blabla\nat io.appmetrica.analytics.push.BlaBla\nat com.android.blabla"
        )
        assertThat(SdkUtils.isExceptionFromPushSdk(throwable)).isTrue()
        assertThat(SdkUtils.isExceptionFromMetrica(throwable)).isFalse()
    }

    @Test
    fun onlyPushWithSuffixCrash() {
        val throwable = TestUtils.createThrowableMock(
            "at com.android.blabla\nat io.appmetrica.analytics.pushsuffix.BlaBla\nat com.android.blabla"
        )
        assertThat(SdkUtils.isExceptionFromPushSdk(throwable)).isTrue()
        assertThat(SdkUtils.isExceptionFromMetrica(throwable)).isFalse()
    }

    @Test
    fun metricaAndPushCrash() {
        val throwable = TestUtils.createThrowableMock(
            "at com.android.blabla\nat io.appmetrica.analytics.BlaBla\nat io.appmetrica.analytics.push.BlaBla"
        )
        assertThat(SdkUtils.isExceptionFromMetrica(throwable)).isTrue()
        assertThat(SdkUtils.isExceptionFromPushSdk(throwable)).isTrue()
    }

    @Test
    fun nonSdkCrash() {
        val throwable = TestUtils.createThrowableMock(
            "at com.android.blabla\nat com.yandex.browser.push.BlaBla\nat com.android.blabla"
        )
        assertThat(SdkUtils.isExceptionFromMetrica(throwable)).isFalse()
        assertThat(SdkUtils.isExceptionFromPushSdk(throwable)).isFalse()
    }

    @Test
    fun logStubUsage() {
        SdkUtils.logStubUsage()
        logMockedStaticRule.staticMock.verify {
            Log.i(SdkUtils.APPMETRICA_TAG, "User is locked. So use stubs. Events will not be sent.")
        }
    }
}
