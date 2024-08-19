package io.appmetrica.analytics.impl

import io.appmetrica.analytics.logger.appmetrica.internal.ImportantLogger
import io.appmetrica.analytics.logger.common.BaseImportantLogger
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.LogRule
import io.appmetrica.analytics.testutils.TestUtils
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SdkUtilsTest : CommonTest() {

    @get:Rule
    val logRule = LogRule()

    @get:Rule
    val logMockedStaticRule = staticRule<BaseImportantLogger>()

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
            ImportantLogger.info(SdkUtils.APPMETRICA_TAG, "User is locked. So use stubs. Events will not be sent.")
        }
    }

    @Test
    fun logAttribution() {
        val message = "message: %s"
        val arg = "ok"
        SdkUtils.logAttribution(message, arg)
        logMockedStaticRule.staticMock.verify {
            ImportantLogger.info("AppMetrica-Attribution", "message: ok")
        }
    }

    @Test
    fun logAttributionE() {
        SdkUtils.logAttributionE(Exception("Some"), "message: %s", "argument")
        logMockedStaticRule.staticMock.verify {
            ImportantLogger.info(
                eq("AppMetrica-Attribution"),
                any()
            )
        }
    }
}
