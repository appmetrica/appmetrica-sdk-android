package io.appmetrica.analytics

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.webkit.WebView
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.ecommerce.ECommerceEvent
import io.appmetrica.analytics.impl.AppMetricaPluginsImplProvider
import io.appmetrica.analytics.impl.AppMetricaPluginsImplProvider.impl
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.TestsData
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy
import io.appmetrica.analytics.impl.proxy.AppMetricaProxyProvider
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.plugins.AppMetricaPlugins
import io.appmetrica.analytics.profile.UserProfile
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.Random
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
internal class AppMetricaTests : CommonTest() {

    private val proxy: AppMetricaProxy = mock()
    private val activity: Activity = mock()
    private val eCommerceEvent: ECommerceEvent = mock()
    private var context: Context = mock()
    private val eventName = "Some event name"
    private val eventValue = "Some event value"
    private val attributes: MutableMap<String, Any> = HashMap()

    private val apiKey = TestsData.UUID_API_KEY

    @get:Rule
    val proxyProviderMockedStaticRule = staticRule<AppMetricaProxyProvider>()

    @get:Rule
    val pluginsImplProviderMockedStaticRule = staticRule<AppMetricaPluginsImplProvider>()

    @Before
    fun setUp() {
        attributes["key"] = 20
        whenever(AppMetricaProxyProvider.getProxy()).thenReturn(proxy)
    }

    @Test
    fun activate() {
        val config = mock<AppMetricaConfig>()
        AppMetrica.activate(context, config)
        verify(proxy).activate(context, config)
    }

    @Test
    fun sendEventsBuffer() {
        AppMetrica.sendEventsBuffer()
        verify(proxy).sendEventsBuffer()
    }

    @Test
    fun resumeSession() {
        AppMetrica.resumeSession(activity)
        verify(proxy).resumeSession(activity)
    }

    @Test
    fun pauseSession() {
        AppMetrica.pauseSession(activity)
        verify(proxy).pauseSession(activity)
    }

    @Test
    fun enableActivityAutoTracking() {
        val application = mock<Application>()
        AppMetrica.enableActivityAutoTracking(application)
        verify(proxy).enableActivityAutoTracking(application)
    }

    @Test
    fun reportEvent() {
        AppMetrica.reportEvent(eventName)
        verify(proxy).reportEvent(eventName)
    }

    @Test
    fun reportError() {
        val throwable = mock<Throwable>()
        AppMetrica.reportError(eventName, throwable)
        verify(proxy).reportError(eventName, throwable)
    }

    @Test
    fun reportCustomError() {
        val id = "ididid"
        val throwable = mock<Throwable>()
        AppMetrica.reportError(id, eventName, throwable)
        verify(proxy).reportError(id, eventName, throwable)
    }

    @Test
    fun reportUnhandledException() {
        val throwable = mock<Throwable>()
        AppMetrica.reportUnhandledException(throwable)
        verify(proxy).reportUnhandledException(throwable)
    }

    @Test
    fun reportEventWithValue() {
        AppMetrica.reportEvent(eventName, eventValue)
        verify(proxy).reportEvent(eventName, eventValue)
    }

    @Test
    fun reportEventWIthAttributes() {
        AppMetrica.reportEvent(eventName, attributes)
        verify(proxy).reportEvent(eventName, attributes)
    }

    @Test
    fun reportAppOpen() {
        AppMetrica.reportAppOpen(activity)
        verify(proxy).reportAppOpen(activity)
    }

    @Test
    fun reportAppOpenIntent() {
        val intent = mock<Intent>()
        AppMetrica.reportAppOpen(intent)
        verify(proxy).reportAppOpen(intent)
    }

    @Test
    fun reportAppOpenDeeplink() {
        val deeplink = "deeplink"
        AppMetrica.reportAppOpen(deeplink)
        verify(proxy).reportAppOpen(deeplink)
    }

    @Test
    fun reportReferralUrl() {
        val url = "referral url"
        AppMetrica.reportReferralUrl(url)
        verify(proxy).reportReferralUrl(url)
    }

    @Test
    fun setLocation() {
        val location = mock<Location>()
        AppMetrica.setLocation(location)
        verify(proxy).setLocation(location)
    }

    @Test
    fun setLocationTrackingEnabled() {
        val enabled = Random().nextBoolean()
        AppMetrica.setLocationTracking(enabled)
        verify(proxy).setLocationTracking(enabled)
    }

    @Test
    fun setLocationTrackingEnabledWithContext() {
        val enabled = Random().nextBoolean()
        AppMetrica.setLocationTracking(context, enabled)
        verify(proxy).setLocationTracking(context, enabled)
    }

    @Test
    fun setStatisticsSending() {
        val enabled = Random().nextBoolean()
        AppMetrica.setStatisticsSending(context, enabled)
        verify(proxy).setStatisticsSending(context, enabled)
    }

    @Test
    fun activateReporter() {
        val reporterConfig = mock<ReporterConfig>()
        AppMetrica.activateReporter(context, reporterConfig)
        verify(proxy).activateReporter(context, reporterConfig)
    }

    @Test
    fun getReporter() {
        val reporter = mock<IReporterExtended>()
        whenever(proxy.getReporter(context, apiKey)).thenReturn(reporter)
        assertThat(AppMetrica.getReporter(context, apiKey)).isEqualTo(reporter)
    }

    @Test
    fun getLibraryVersion() {
        assertThat(AppMetrica.getLibraryVersion()).isEqualTo(BuildConfig.VERSION_NAME)
    }

    @Test
    fun getLibraryApiLevel() {
        assertThat(AppMetrica.getLibraryApiLevel()).isEqualTo(BuildConfig.API_LEVEL)
    }

    @Test
    fun requestDeferredDeeplinkParameters() {
        val listener = mock<DeferredDeeplinkParametersListener>()
        AppMetrica.requestDeferredDeeplinkParameters(listener)
        verify(proxy).requestDeferredDeeplinkParameters(listener)
    }

    @Test
    fun requestDeferredDeeplink() {
        val listener = mock<DeferredDeeplinkListener>()
        AppMetrica.requestDeferredDeeplink(listener)
        verify(proxy).requestDeferredDeeplink(listener)
    }

    @Test
    fun setUserProfileId() {
        val id = "some id"
        AppMetrica.setUserProfileID(id)
        verify(proxy).setUserProfileID(id)
    }

    @Test
    fun reportUserProfile() {
        val userProfile = mock<UserProfile>()
        AppMetrica.reportUserProfile(userProfile)
        verify(proxy).reportUserProfile(userProfile)
    }

    @Test
    fun reportRevenue() {
        val revenue = mock<Revenue>()
        AppMetrica.reportRevenue(revenue)
        verify(proxy).reportRevenue(revenue)
    }

    @Test
    fun reportAdRevenue() {
        val revenue = mock<AdRevenue>()
        AppMetrica.reportAdRevenue(revenue)
        verify(proxy).reportAdRevenue(revenue)
    }

    @Test
    fun putErrorEnvironmentValue() {
        val key = "key"
        val value = "value"
        AppMetrica.putErrorEnvironmentValue(key, value)
        verify(proxy).putErrorEnvironmentValue(key, value)
    }

    @Test
    fun reportECommerce() {
        AppMetrica.reportECommerce(eCommerceEvent)
        verify(proxy).reportECommerce(eCommerceEvent)
    }

    @Test
    fun initWebViewReporting() {
        val webView = mock<WebView>()
        AppMetrica.initWebViewReporting(webView)
        verify(proxy).initWebViewReporting(webView)
    }

    @Test
    fun putAppEnvironmentValue() {
        val key = "appEnvironmentKey"
        val value = "appEnvironmentValue"
        AppMetrica.putAppEnvironmentValue(key, value)
        verify(proxy).putAppEnvironmentValue(key, value)
    }

    @Test
    fun clearAppEnvironment() {
        AppMetrica.clearAppEnvironment()
        verify(proxy).clearAppEnvironment()
    }

    @Test
    fun getPluginExtension() {
        val pluginsImpl = mock<AppMetricaPlugins>()
        whenever(impl).thenReturn(pluginsImpl)
        assertThat(AppMetrica.getPluginExtension()).isSameAs(pluginsImpl)
    }

    @Test
    fun getDeviceId() {
        val deviceId = "888999777666"
        whenever(proxy.deviceId).thenReturn(deviceId)
        assertThat(AppMetrica.getDeviceId(context)).isEqualTo(deviceId)
    }

    @Test
    fun getUuid() {
        val uuid = UUID.randomUUID().toString()
        val uuidResult = IdentifiersResult(uuid, IdentifierStatus.OK, null)
        whenever(proxy.getUuid(context)).thenReturn(uuidResult)
        assertThat(AppMetrica.getUuid(context)).isEqualTo(uuid)
    }

    @Test
    fun getUuidForNullValue() {
        val identifiersResult = IdentifiersResult(null, IdentifierStatus.OK, null)
        whenever(proxy.getUuid(context)).thenReturn(identifiersResult)
        assertThat(AppMetrica.getUuid(context)).isNull()
    }

    @Test
    fun registerAnrListener() {
        val listener = mock<AnrListener>()
        AppMetrica.registerAnrListener(listener)
        verify(proxy).registerAnrListener(eq(listener))
    }
}
