package io.appmetrica.analytics.impl.proxy.validation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.webkit.WebView
import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.AnrListener
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.DeferredDeeplinkListener
import io.appmetrica.analytics.DeferredDeeplinkParametersListener
import io.appmetrica.analytics.ExternalAttribution
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.Revenue
import io.appmetrica.analytics.StartupParamsCallback
import io.appmetrica.analytics.ValidationException
import io.appmetrica.analytics.ecommerce.ECommerceEvent
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.profile.UserProfile
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class BarrierTest : CommonTest() {

    private val appMetricaFacadeProvider: AppMetricaFacadeProvider = mock {
        on { isActivated } doReturn true
    }

    private val mBarrier = Barrier(appMetricaFacadeProvider)

    @Test
    fun enableActivityAutoTracking() {
        mBarrier.enableActivityAutoTracking(mock<Application>())
    }

    @Test(expected = ValidationException::class)
    fun enableActivityAutoTrackingIfApplicationIsNull() {
        mBarrier.enableActivityAutoTracking(null)
    }

    @Test
    fun reportAppOpen() {
        mBarrier.reportAppOpen(mock<Activity>())
    }

    @Test(expected = ValidationException::class)
    fun reportAppOpenIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.reportAppOpen(mock<Activity>())
    }

    @Test(expected = ValidationException::class)
    fun reportAppOpenIfActivityIsNull() {
        mBarrier.reportAppOpen(null as Activity?)
    }

    @Test
    fun reportAppOpenWithDeeplink() {
        mBarrier.reportAppOpen("deeplink")
    }

    @Test(expected = ValidationException::class)
    fun reportAppOpenWithDeeplinkIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.reportAppOpen("deeplink")
    }

    @Test(expected = ValidationException::class)
    fun reportAppOpenWithDeeplinkIfDeeplinkIsEmpty() {
        mBarrier.reportAppOpen("")
    }

    @Test(expected = ValidationException::class)
    fun reportAppOpenWithDeeplinkIfDeeplinkIsNull() {
        mBarrier.reportAppOpen(null as String?)
    }

    @Test
    fun reportAppOpenWithIntent() {
        mBarrier.reportAppOpen(mock<Intent>())
    }

    @Test(expected = ValidationException::class)
    fun reportAppOpenWithIntentIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.reportAppOpen(mock<Intent>())
    }

    @Test(expected = ValidationException::class)
    fun reportAppOpenWithIntentIfIntentIsNull() {
        mBarrier.reportAppOpen(null as Intent?)
    }

    @Test
    fun reportReferralUrl() {
        mBarrier.reportReferralUrl("ReferralUrl")
    }

    @Test(expected = ValidationException::class)
    fun reportReferralUrlIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.reportReferralUrl("ReferralUrl")
    }

    @Test(expected = ValidationException::class)
    fun reportReferralUrlIfUrlIsEmpty() {
        mBarrier.reportReferralUrl("")
    }

    @Test(expected = ValidationException::class)
    fun reportReferralUrlIfUrlIsNull() {
        mBarrier.reportReferralUrl(null)
    }

    @Test
    fun setLocation() {
        mBarrier.setLocation(mock<Location>())
    }

    @Test
    fun setLocationTracking() {
        mBarrier.setLocationTracking(true)
    }

    @Test
    fun requestDeferredDeeplinkParameters() {
        mBarrier.requestDeferredDeeplinkParameters(mock<DeferredDeeplinkParametersListener>())
    }

    @Test(expected = ValidationException::class)
    fun requestDeferredDeeplinkParametersIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.requestDeferredDeeplinkParameters(mock<DeferredDeeplinkParametersListener>())
    }

    @Test(expected = ValidationException::class)
    fun requestDeferredDeeplinkParametersIfListenerIsNull() {
        mBarrier.requestDeferredDeeplinkParameters(null)
    }

    @Test
    fun requestDeferredDeeplink() {
        mBarrier.requestDeferredDeeplink(mock<DeferredDeeplinkListener>())
    }

    @Test(expected = ValidationException::class)
    fun requestDeferredDeeplinkIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.requestDeferredDeeplink(mock<DeferredDeeplinkListener>())
    }

    @Test(expected = ValidationException::class)
    fun requestDeferredDeeplinkIfListenerIsNull() {
        mBarrier.requestDeferredDeeplink(null)
    }

    @Test
    fun setDataSendingEnabled() {
        mBarrier.setDataSendingEnabled(false)
    }

    @Test
    fun getReporter() {
        mBarrier.getReporter(mock<Context>(), UUID.randomUUID().toString())
    }

    @Test(expected = ValidationException::class)
    fun getReporterIfContextIsNull() {
        mBarrier.getReporter(null, "apiKey")
    }

    @Test(expected = ValidationException::class)
    fun getReporterIfKeyIsInvalid() {
        mBarrier.getReporter(mock<Context>(), "apiKey")
    }

    @Test(expected = ValidationException::class)
    fun getReporterIfKeyIsEmpty() {
        mBarrier.getReporter(mock<Context>(), "")
    }

    @Test(expected = ValidationException::class)
    fun getReporterIfKeyIsNull() {
        mBarrier.getReporter(mock<Context>(), null)
    }

    @Test
    fun activateReporter() {
        mBarrier.activateReporter(mock<Context>(), mock<ReporterConfig>())
    }

    @Test(expected = ValidationException::class)
    fun activateReporterIfContextIsNull() {
        mBarrier.activateReporter(null, mock<ReporterConfig>())
    }

    @Test(expected = ValidationException::class)
    fun activateReporterIfConfigIsNull() {
        mBarrier.activateReporter(mock<Context>(), null)
    }

    @Test
    fun activate() {
        mBarrier.activate(
            mock<Context>(),
            AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build()
        )
    }

    @Test(expected = ValidationException::class)
    fun activateIfContextIsNull() {
        mBarrier.activate(null, AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build())
    }

    @Test(expected = ValidationException::class)
    fun activateIfConfigIsNull() {
        mBarrier.activate(mock<Context>(), null)
    }

    @Test
    fun putErrorEnvironmentValue() {
        mBarrier.putErrorEnvironmentValue("key", "value")
    }

    @Test
    fun putErrorEnvironmentValueIfValueIsNull() {
        mBarrier.putErrorEnvironmentValue("key", null)
    }

    @Test(expected = ValidationException::class)
    fun putErrorEnvironmentValueIfKeyIsNull() {
        mBarrier.putErrorEnvironmentValue(null, "value")
    }

    @Test
    fun initWebViewReporting() {
        mBarrier.initWebViewReporting(mock<WebView>())
    }

    @Test(expected = ValidationException::class)
    fun initWebViewReportingIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.initWebViewReporting(mock<WebView>())
    }

    @Test(expected = ValidationException::class)
    fun initWebViewReportingIfWebViewIsNull() {
        mBarrier.initWebViewReporting(null)
    }

    @Test
    fun reportJsEvent() {
        assertThat(mBarrier.reportJsEvent("name", "value")).isTrue()
    }

    @Test(expected = ValidationException::class)
    fun reportJsEventIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        assertThat(mBarrier.reportJsEvent("name", "value")).isTrue()
    }

    @Test
    fun reportJsEventIfValueIsNull() {
        assertThat(mBarrier.reportJsEvent("name", null)).isTrue()
    }

    @Test
    fun reportJsEventIfNameIsNull() {
        assertThat(mBarrier.reportJsEvent(null, "value")).isFalse()
    }

    @Test
    fun reportJsEventIfNameIsEmpty() {
        assertThat(mBarrier.reportJsEvent("", "value")).isFalse()
    }

    @Test
    fun reportJsInitEvent() {
        assertThat(mBarrier.reportJsInitEvent("aaa")).isTrue()
    }

    @Test
    fun reportJsInitEventIfValueIsEmpty() {
        assertThat(mBarrier.reportJsInitEvent("")).isFalse()
    }

    @Test
    fun reportJsInitEventIfValueIsNull() {
        assertThat(mBarrier.reportJsInitEvent(null)).isFalse()
    }

    @Test
    fun requestStartupParams() {
        mBarrier.requestStartupParams(
            mock<Context>(),
            mock<StartupParamsCallback>(),
            emptyList()
        )
    }

    @Test(expected = ValidationException::class)
    fun requestStartupParamsIfContextIsNull() {
        mBarrier.requestStartupParams(
            null,
            mock<StartupParamsCallback>(),
            emptyList()
        )
    }

    @Test(expected = ValidationException::class)
    fun requestStartupParamsIfCallbackIsNull() {
        mBarrier.requestStartupParams(
            mock<Context>(),
            null,
            emptyList()
        )
    }

    @Test
    fun requestStartupParamsIfListIsNull() {
        mBarrier.requestStartupParams(
            mock<Context>(),
            mock<StartupParamsCallback>(),
            null as List<String?>?
        )
    }

    @Test
    fun getUuid() {
        mBarrier.getUuid(mock<Context>())
    }

    @Test(expected = ValidationException::class)
    fun getUuidIfContextIsNull() {
        mBarrier.getUuid(null)
    }

    @Test
    fun registerAnrListener() {
        mBarrier.registerAnrListener(mock<AnrListener>())
    }

    @Test(expected = ValidationException::class)
    fun registerAnrListenerIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.registerAnrListener(mock<AnrListener>())
    }

    @Test(expected = ValidationException::class)
    fun registerAnrListenerIfListenerIsNull() {
        mBarrier.registerAnrListener(null)
    }

    @Test
    fun reportExternalAttribution() {
        mBarrier.reportExternalAttribution(mock<ExternalAttribution>())
    }

    @Test(expected = ValidationException::class)
    fun reportExternalAttributionIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.reportExternalAttribution(mock<ExternalAttribution>())
    }

    @Test(expected = ValidationException::class)
    fun reportExternalAttributionIfAttributionIsNull() {
        mBarrier.reportExternalAttribution(null)
    }

    @Test
    fun reportEvent() {
        mBarrier.reportEvent("event")
    }

    @Test(expected = ValidationException::class)
    fun reportEventIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
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
    fun reportEventWithJsonIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
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
    fun reportEventWithAttributesIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
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
    fun reportErrorIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
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
    fun reportErrorWithMessageAndThrowableIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
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
    fun reportUnhandledExceptionWithThrowableIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.reportUnhandledException(mock<Throwable>())
    }

    @Test(expected = ValidationException::class)
    fun reportUnhandledExceptionWithThrowableIfThrowableIsNull() {
        mBarrier.reportUnhandledException(null as Throwable?)
    }

    @Test
    fun resumeSession() {
        mBarrier.resumeSession()
    }

    @Test(expected = ValidationException::class)
    fun resumeSessionIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.resumeSession()
    }

    @Test
    fun pauseSession() {
        mBarrier.pauseSession()
    }

    @Test(expected = ValidationException::class)
    fun pauseSessionIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
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
    fun reportUserProfileIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
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
    fun reportRevenueIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
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
    fun reportECommerceIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.reportECommerce(mock<ECommerceEvent>())
    }

    @Test(expected = ValidationException::class)
    fun reportECommerceIfECommerceIsNull() {
        mBarrier.reportECommerce(null)
    }

    @Test
    fun reportAdRevenue() {
        mBarrier.reportAdRevenue(mock<AdRevenue>())
    }

    @Test(expected = ValidationException::class)
    fun reportAdRevenueIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
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

    @Test(expected = ValidationException::class)
    fun sendEventsBufferIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.sendEventsBuffer()
    }

    @Test
    fun reportExternalAdRevenue() {
        mBarrier.reportExternalAdRevenue("string")
    }

    @Test(expected = ValidationException::class)
    fun reportExternalAdRevenueIfNotActivated() {
        whenever(appMetricaFacadeProvider.isActivated).thenReturn(false)
        mBarrier.reportExternalAdRevenue("string")
    }

    @Test
    fun `reportAnr for valid values`() {
        mBarrier.reportAnr(mapOf(mock<Thread>() to arrayOf(mock<StackTraceElement>())))
    }

    @Test(expected = ValidationException::class)
    fun `reportAnr for null all threads`() {
        mBarrier.reportAnr(null)
    }
}
