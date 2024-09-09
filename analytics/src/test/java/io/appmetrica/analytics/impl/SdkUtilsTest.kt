package io.appmetrica.analytics.impl

import io.appmetrica.analytics.logger.appmetrica.internal.ImportantLogger
import io.appmetrica.analytics.logger.common.BaseImportantLogger
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.LogRule
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SdkUtilsTest : CommonTest() {

    @get:Rule
    val logRule = LogRule()

    @get:Rule
    val logMockedStaticRule = staticRule<BaseImportantLogger>()

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
