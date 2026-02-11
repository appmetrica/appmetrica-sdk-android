package io.appmetrica.analytics.impl.proxy.validation

import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.Revenue
import io.appmetrica.analytics.ValidationException
import io.appmetrica.analytics.ecommerce.ECommerceEvent
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException
import io.appmetrica.analytics.profile.UserProfile
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock

internal class ReporterBarrierTest : CommonTest() {

    private val mBarrier = ReporterBarrier()

    @Test
    fun reportEvent() {
        mBarrier.reportEvent("event")
    }

    @Test(expected = ValidationException::class)
    fun reportEventIfNameIsEmpty() {
        mBarrier.reportEvent("")
    }

    @Test(expected = ValidationException::class)
    fun reportEventIfNameIsNull() {
        mBarrier.reportEvent(null as String?)
    }

    @Test
    fun reportEventWithJson() {
        mBarrier.reportEvent("name", "json")
    }

    @Test(expected = ValidationException::class)
    fun reportEventWithJsonIfNameIsEmpty() {
        mBarrier.reportEvent("", "json")
    }

    @Test(expected = ValidationException::class)
    fun reportEventWithJsonIfNameIsNull() {
        mBarrier.reportEvent(null, "json")
    }

    @Test
    fun reportEventWithAttributes() {
        mBarrier.reportEvent("name", emptyMap())
    }

    @Test(expected = ValidationException::class)
    fun reportEventWithAttributesIfNameIsEmpty() {
        mBarrier.reportEvent("", emptyMap())
    }

    @Test(expected = ValidationException::class)
    fun reportEventWithAttributesIfNameIsNull() {
        mBarrier.reportEvent(null, emptyMap())
    }

    @Test
    fun reportError() {
        mBarrier.reportError("native crash", null as Throwable?)
    }

    @Test(expected = ValidationException::class)
    fun reportErrorIfNameIsEmpty() {
        mBarrier.reportError("", null as Throwable?)
    }

    @Test(expected = ValidationException::class)
    fun reportErrorIfNameIsNull() {
        mBarrier.reportError(null, null as Throwable?)
    }

    @Test
    fun reportErrorWithMessageAndThrowable() {
        mBarrier.reportError("id", null, null)
    }

    @Test(expected = ValidationException::class)
    fun reportErrorWithMessageAndThrowableIfNameIsEmpty() {
        mBarrier.reportError("", null, null)
    }

    @Test(expected = ValidationException::class)
    fun reportErrorWithMessageAndThrowableIfNameIsNull() {
        mBarrier.reportError(null, null, null)
    }

    @Test
    fun reportUnhandledExceptionWithThrowable() {
        mBarrier.reportUnhandledException(mock<Throwable>())
    }

    @Test(expected = ValidationException::class)
    fun reportUnhandledExceptionWithThrowableIfThrowableIsNull() {
        mBarrier.reportUnhandledException(null as Throwable?)
    }

    @Test
    fun reportUnhandledException() {
        mBarrier.reportUnhandledException(mock<UnhandledException>())
    }

    @Test(expected = ValidationException::class)
    fun reportUnhandledExceptionIfUnhandledExceptionIsNull() {
        mBarrier.reportUnhandledException(null as UnhandledException?)
    }

    @Test
    fun resumeSession() {
        mBarrier.resumeSession()
    }

    @Test
    fun pauseSession() {
        mBarrier.pauseSession()
    }

    @Test
    fun setUserProfileID() {
        mBarrier.setUserProfileID("")
    }

    @Test
    fun reportUserProfile() {
        mBarrier.reportUserProfile(mock<UserProfile>())
    }

    @Test(expected = ValidationException::class)
    fun reportUserProfileIfUserProfileIsNull() {
        mBarrier.reportUserProfile(null)
    }

    @Test
    fun reportRevenue() {
        mBarrier.reportRevenue(mock<Revenue>())
    }

    @Test(expected = ValidationException::class)
    fun reportRevenueIfRevenueIsNull() {
        mBarrier.reportRevenue(null)
    }

    @Test
    fun reportECommerce() {
        mBarrier.reportECommerce(mock<ECommerceEvent>())
    }

    @Test(expected = ValidationException::class)
    fun reportECommerceIfECommerceIsNull() {
        mBarrier.reportECommerce(null)
    }

    @Test
    fun setDataSendingEnabled() {
        mBarrier.setDataSendingEnabled(true)
    }

    @Test
    fun reportAdRevenue() {
        mBarrier.reportAdRevenue(mock<AdRevenue>())
    }

    @Test(expected = ValidationException::class)
    fun reportAdRevenueIfAdRevenueIsNull() {
        mBarrier.reportAdRevenue(null)
    }

    @Test
    fun putAppEnvironmentValue() {
        mBarrier.putAppEnvironmentValue("key", "value")
    }

    @Test
    fun clearAppEnvironment() {
        mBarrier.clearAppEnvironment()
    }

    @Test
    fun sendEventsBuffer() {
        mBarrier.sendEventsBuffer()
    }

    @Test
    fun reportAnr() {
        mBarrier.reportAnr(mock<AllThreads>())
    }

    @Test
    fun `reportAnr from api for valid values`() {
        mBarrier.reportAnr(mock<Map<Thread, Array<StackTraceElement>>>())
    }

    @Test(expected = ValidationException::class)
    fun `reportAnr from api for null all threads`() {
        mBarrier.reportAnr(null as Map<Thread, Array<StackTraceElement>>?)
    }

    @Test
    fun activate() {
        mBarrier.activate(mock<ReporterConfig>())
    }

    @Test
    fun reportEventWithModuleEvent() {
        mBarrier.reportEvent(mock<ModuleEvent>())
    }

    @Test
    fun setSessionExtra() {
        mBarrier.setSessionExtra("key", "value".toByteArray())
    }

    @Test
    fun reportAdRevenueWithBoolean() {
        mBarrier.reportAdRevenue(mock<AdRevenue>(), true)
    }

    @Test(expected = ValidationException::class)
    fun reportAdRevenueWithBooleanIfAdRevenueIsNull() {
        mBarrier.reportAdRevenue(null, true)
    }
}
