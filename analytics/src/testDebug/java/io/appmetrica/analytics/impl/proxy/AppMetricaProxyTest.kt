package io.appmetrica.analytics.impl.proxy

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
import io.appmetrica.analytics.coreapi.event.AppMetricaEvent
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.validation.ValidationResult
import io.appmetrica.analytics.coreutils.internal.validation.Validator
import io.appmetrica.analytics.ecommerce.ECommerceEvent
import io.appmetrica.analytics.impl.ActivityLifecycleManager
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.DeeplinkConsumer
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig
import io.appmetrica.analytics.impl.MainReporter
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider
import io.appmetrica.analytics.impl.SessionsTrackingManager
import io.appmetrica.analytics.impl.TestsData
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler
import io.appmetrica.analytics.impl.proxy.synchronous.SynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.Barrier
import io.appmetrica.analytics.impl.proxy.validation.SilentActivationValidator
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor
import io.appmetrica.analytics.profile.UserProfile
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.MockProvider
import io.appmetrica.gradle.androidtestutils.rules.ContextRule
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.same
import org.mockito.kotlin.whenever

internal class AppMetricaProxyTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val mainReporter: MainReporter = mock()
    private val mainReporterApiConsumerProvider: MainReporterApiConsumerProvider = mock()
    private val deeplinkConsumer: DeeplinkConsumer = mock()
    private val impl: AppMetricaFacade = mock()
    private val provider: AppMetricaFacadeProvider = mock()
    private val reporterProxyStorage: ReporterProxyStorage = mock()
    private val barrier: Barrier = mock()
    private val applicationContext: Context = mock()
    private val throwable: Throwable = mock()
    private val synchronousStageExecutor: SynchronousStageExecutor = mock()
    private val defaultOneShotMetricaConfig: DefaultOneShotMetricaConfig = mock()
    private val eCommerceEvent: ECommerceEvent = mock()
    private val webViewJsInterfaceHandler: WebViewJsInterfaceHandler = mock()
    private val silentActivationValidator: SilentActivationValidator = mock()
    private val sessionsTrackingManager: SessionsTrackingManager = mock()

    private lateinit var context: Context
    private lateinit var blockedExecutor: IHandlerExecutor
    private lateinit var proxy: AppMetricaProxy

    @Before
    fun setUp() {
        context = contextRule.context
        whenever(context.applicationContext).thenReturn(applicationContext)
        blockedExecutor = MockProvider.mockedBlockingExecutorMock()
        whenever(provider.peekInitializedImpl()).thenReturn(impl)
        whenever(provider.getInitializedImpl(applicationContext)).thenReturn(impl)
        whenever(ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor).thenReturn(blockedExecutor)
        whenever(synchronousStageExecutor.reportError(any(), anyOrNull())).thenReturn(mock())
        whenever(sessionsTrackingManager.startWatchingIfNotYet())
            .thenReturn(ActivityLifecycleManager.WatchingStatus.WATCHING)
        whenever(reporterProxyStorage.getOrCreate(any<Context>(), any<String>())).thenReturn(mock())
        whenever(
            ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(applicationContext).readUuid()
        ).thenReturn(mock())
        proxy = createProxy()
        whenever(impl.mainReporterApiConsumerProvider).thenReturn(mainReporterApiConsumerProvider)
        doReturn(mainReporter).whenever(mainReporterApiConsumerProvider).mainReporter
        doReturn(deeplinkConsumer).whenever(mainReporterApiConsumerProvider).deeplinkConsumer
    }

    @Test
    fun defaultConstructor() {
        proxy = AppMetricaProxy()
        assertThat(proxy).isNotNull()
    }

    @Test
    fun activate() {
        val config: AppMetricaConfig = mock()
        val mergedConfig: AppMetricaConfig = mock()
        whenever(defaultOneShotMetricaConfig.mergeWithUserConfig(config)).thenReturn(mergedConfig)
        proxy.activate(context, config)
        val order = inOrder(barrier, synchronousStageExecutor, provider)
        order.verify(barrier).activate(context, config)
        order.verify(synchronousStageExecutor).activate(applicationContext, config)
        order.verify(provider).markActivated()
        verify(impl).activateFull(mergedConfig)
    }

    @Test
    fun resumeSession() {
        val activity: Activity = mock()
        proxy.resumeSession(activity)
        verify(barrier).resumeSession()
        val order = inOrder(barrier, synchronousStageExecutor, sessionsTrackingManager)
        order.verify(barrier).resumeSession()
        order.verify(synchronousStageExecutor).resumeSession(activity)
        order.verify(sessionsTrackingManager).resumeActivityManually(activity, mainReporter)
    }

    @Test
    fun pauseSession() {
        val activity: Activity = mock()
        proxy.pauseSession(activity)
        val order = inOrder(barrier, synchronousStageExecutor, sessionsTrackingManager)
        order.verify(barrier).pauseSession()
        order.verify(synchronousStageExecutor).pauseSession(activity)
        order.verify(sessionsTrackingManager).pauseActivityManually(activity, mainReporter)
    }

    @Test
    fun enableActivityAutoTracking() {
        val application: Application = mock()
        val status = ActivityLifecycleManager.WatchingStatus.WATCHING
        whenever(sessionsTrackingManager.startWatchingIfNotYet()).thenReturn(status)
        proxy.enableActivityAutoTracking(application)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).enableActivityAutoTracking(application)
        order.verify(synchronousStageExecutor).enableActivityAutoTracking(application)
        order.verify(mainReporter).onEnableAutoTrackingAttemptOccurred(status)
    }

    @Test
    fun reportEvent() {
        val name = "eventName"
        proxy.reportEvent(name)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportEvent(name)
        order.verify(synchronousStageExecutor).reportEvent(name)
        order.verify(mainReporter).reportEvent(name)
    }

    @Test
    fun reportEventWithJson() {
        val name = "eventName"
        val json = "json"
        proxy.reportEvent(name, json)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportEvent(name, json)
        order.verify(synchronousStageExecutor).reportEvent(name, json)
        order.verify(mainReporter).reportEvent(name, json)
    }

    @Test
    fun reportEventWithMap() {
        val name = "eventName"
        val map = mutableMapOf<String, Any?>("a" to Any())
        proxy.reportEvent(name, map)
        val mapCaptor = argumentCaptor<LinkedHashMap<String, Any?>>()
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportEvent(name, map)
        order.verify(synchronousStageExecutor).reportEvent(name, map)
        order.verify(mainReporter).reportEvent(eq(name), mapCaptor.capture())
        assertThat(mapCaptor.firstValue).isEqualTo(map)
    }

    @Test
    fun reportEventWithArrayMap() {
        val attributes = mutableMapOf<String, Any?>("k1" to "v1", "k2" to "v2")
        val name = "eventName"
        proxy.reportEvent(name, attributes)
        val mapCaptor = argumentCaptor<LinkedHashMap<String, Any?>>()
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportEvent(name, attributes)
        order.verify(synchronousStageExecutor).reportEvent(name, attributes)
        order.verify(mainReporter).reportEvent(eq(name), mapCaptor.capture())
        assertThat(mapCaptor.firstValue).isEqualTo(attributes)
    }

    @Test
    fun reportEventWithNullMap() {
        val name = "eventName"
        val attributes: Map<String, Any?>? = null
        proxy.reportEvent(name, attributes)
        val mapCaptor = argumentCaptor<LinkedHashMap<String, Any?>>()
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportEvent(name, attributes)
        order.verify(synchronousStageExecutor).reportEvent(name, attributes)
        order.verify(mainReporter).reportEvent(eq(name), mapCaptor.capture())
        assertThat(mapCaptor.firstValue).isEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun reportEventWithNullableEntries() {
        val name = "eventName"
        val attributes = mapOf(null to null) as Map<String, Any?>
        proxy.reportEvent(name, attributes)
        val mapCaptor = argumentCaptor<LinkedHashMap<String, Any?>>()
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportEvent(name, attributes)
        order.verify(synchronousStageExecutor).reportEvent(name, attributes)
        order.verify(mainReporter).reportEvent(eq(name), mapCaptor.capture())
        assertThat(mapCaptor.firstValue).containsEntry(null, null)
    }

    @Test
    fun reportEventWithEmptyMap() {
        val name = "eventName"
        val map = emptyMap<String, Any?>()
        proxy.reportEvent(name, map)
        val mapCaptor = argumentCaptor<LinkedHashMap<String, Any?>>()
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportEvent(name, map)
        order.verify(synchronousStageExecutor).reportEvent(name, map)
        order.verify(mainReporter).reportEvent(eq(name), mapCaptor.capture())
        assertThat(mapCaptor.firstValue).isEmpty()
    }

    @Test
    fun reportEventWithAttributesMapChanged() {
        val executor: IHandlerExecutor = mock()
        whenever(ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor).thenReturn(executor)
        proxy = createProxy()
        val name = "name"
        val attributes = mutableMapOf<String, Any?>("k1" to "v1")
        proxy.reportEvent(name, attributes)
        val runnableCaptor = argumentCaptor<Runnable>()
        verify(executor).execute(runnableCaptor.capture())
        attributes["k1"] = "v2"
        runnableCaptor.firstValue.run()
        verify(mainReporter).reportEvent(eq(name), argThat<MutableMap<String, Any?>> { get("k1") == "v1" && size == 1 })
    }

    @Test
    fun reportUnhandledException() {
        val exception: Throwable = mock()
        proxy.reportUnhandledException(exception)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportUnhandledException(exception)
        order.verify(synchronousStageExecutor).reportUnhandledException(exception)
        order.verify(mainReporter).reportUnhandledException(exception)
    }

    @Test
    fun reportAppOpen() {
        val activity: Activity = mock()
        val intent: Intent = mock()
        whenever(synchronousStageExecutor.reportAppOpen(activity)).thenReturn(intent)
        proxy.reportAppOpen(activity)
        val order = inOrder(barrier, synchronousStageExecutor, deeplinkConsumer)
        order.verify(barrier).reportAppOpen(activity)
        order.verify(synchronousStageExecutor).reportAppOpen(activity)
        order.verify(deeplinkConsumer).reportAppOpen(intent)
    }

    @Test
    fun reportAppOpenNullIntent() {
        doReturn(true).whenever(provider).isActivated
        val activity: Activity = mock()
        whenever(synchronousStageExecutor.reportAppOpen(activity)).thenReturn(null)
        proxy.reportAppOpen(activity)
        val order = inOrder(barrier, synchronousStageExecutor, deeplinkConsumer)
        order.verify(barrier).reportAppOpen(activity)
        order.verify(synchronousStageExecutor).reportAppOpen(activity)
        order.verify(deeplinkConsumer).reportAppOpen(null as Intent?)
    }

    @Test
    fun reportAppOpenIntent() {
        val intent: Intent = mock()
        proxy.reportAppOpen(intent)
        val order = inOrder(barrier, synchronousStageExecutor, deeplinkConsumer)
        order.verify(barrier).reportAppOpen(intent)
        order.verify(synchronousStageExecutor).reportAppOpen(intent)
        order.verify(deeplinkConsumer).reportAppOpen(intent)
    }

    @Test
    fun reportAppOpenString() {
        val appOpen = "appOpen"
        proxy.reportAppOpen(appOpen)
        val order = inOrder(barrier, synchronousStageExecutor, deeplinkConsumer)
        order.verify(barrier).reportAppOpen(appOpen)
        order.verify(synchronousStageExecutor).reportAppOpen(appOpen)
        order.verify(deeplinkConsumer).reportAppOpen(appOpen)
    }

    @Test
    fun setLocation() {
        val location: Location = mock()
        proxy.setLocation(location)
        val order = inOrder(barrier, synchronousStageExecutor, provider)
        order.verify(barrier).setLocation(location)
        order.verify(synchronousStageExecutor).setLocation(location)
        order.verify(provider).setLocation(location)
    }

    @Test
    fun setLocationTrackingForNonInitialized() {
        proxy.setLocationTracking(true)
        val order = inOrder(barrier, synchronousStageExecutor, provider)
        order.verify(barrier).setLocationTracking(true)
        order.verify(synchronousStageExecutor).setLocationTracking(true)
        order.verify(provider).setLocationTracking(true)
    }

    @Test
    fun setAdvIdentifiersTracking() {
        proxy.setAdvIdentifiersTracking(true)
        val order = inOrder(barrier, synchronousStageExecutor, provider)
        order.verify(barrier).setAdvIdentifiersTracking(true)
        order.verify(synchronousStageExecutor).setAdvIdentifiersTracking(true)
        order.verify(provider).setAdvIdentifiersTracking(true)
        order.verifyNoMoreInteractions()
    }

    @Test
    fun requestDeferredDeeplinkParameters() {
        val listener: DeferredDeeplinkParametersListener = mock()
        proxy.requestDeferredDeeplinkParameters(listener)
        val order = inOrder(barrier, synchronousStageExecutor, impl)
        order.verify(barrier).requestDeferredDeeplinkParameters(listener)
        order.verify(synchronousStageExecutor).requestDeferredDeeplinkParameters(listener)
        order.verify(impl).requestDeferredDeeplinkParameters(listener)
    }

    @Test
    fun requestDeferredDeeplink() {
        val listener: DeferredDeeplinkListener = mock()
        proxy.requestDeferredDeeplink(listener)
        val order = inOrder(barrier, synchronousStageExecutor, impl)
        order.verify(barrier).requestDeferredDeeplink(listener)
        order.verify(synchronousStageExecutor).requestDeferredDeeplink(listener)
        order.verify(impl).requestDeferredDeeplink(listener)
    }

    @Test
    fun setUserProfileID() {
        doReturn(true).whenever(provider).isActivated
        val userProfileID = "userProfileID"
        proxy.setUserProfileID(userProfileID)
        val order = inOrder(barrier, synchronousStageExecutor, provider, mainReporter)
        order.verify(barrier).setUserProfileID(userProfileID)
        order.verify(synchronousStageExecutor).setUserProfileID(userProfileID)
        order.verify(provider).setUserProfileID(userProfileID)
        order.verifyNoMoreInteractions()
    }

    fun setUserProfileIDNonActivated() {
        doReturn(false).whenever(provider).isActivated
        val userProfileID = "userProfileID"
        proxy.setUserProfileID(userProfileID)
        val order = inOrder(barrier, synchronousStageExecutor, provider, mainReporter)
        order.verify(barrier).setUserProfileID(userProfileID)
        order.verify(synchronousStageExecutor).setUserProfileID(userProfileID)
        order.verify(provider).setUserProfileID(userProfileID)
        order.verifyNoMoreInteractions()
    }

    @Test
    fun reportUserProfile() {
        val userProfile: UserProfile = mock()
        proxy.reportUserProfile(userProfile)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportUserProfile(userProfile)
        order.verify(synchronousStageExecutor).reportUserProfile(userProfile)
        order.verify(mainReporter).reportUserProfile(userProfile)
    }

    @Test
    fun reportRevenue() {
        val revenue: Revenue = mock()
        proxy.reportRevenue(revenue)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportRevenue(revenue)
        order.verify(synchronousStageExecutor).reportRevenue(revenue)
        order.verify(mainReporter).reportRevenue(revenue)
    }

    @Test
    fun reportAdRevenue() {
        val revenue: AdRevenue = mock()
        proxy.reportAdRevenue(revenue)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportAdRevenue(revenue)
        order.verify(synchronousStageExecutor).reportAdRevenue(revenue)
        order.verify(mainReporter).reportAdRevenue(revenue)
    }

    @Test
    fun reportECommerce() {
        proxy.reportECommerce(eCommerceEvent)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportECommerce(eCommerceEvent)
        order.verify(synchronousStageExecutor).reportECommerce(eCommerceEvent)
        order.verify(mainReporter).reportECommerce(eCommerceEvent)
        order.verifyNoMoreInteractions()
    }

    @Test
    fun getReporter() {
        val apiKey = TestsData.generateApiKey()
        val reporter: ReporterExtendedProxy = mock()
        whenever(reporterProxyStorage.getOrCreate(applicationContext, apiKey)).thenReturn(reporter)
        assertThat(proxy.getReporter(context, apiKey)).isSameAs(reporter)
        val order = inOrder(barrier, synchronousStageExecutor, reporterProxyStorage)
        order.verify(barrier).getReporter(context, apiKey)
        order.verify(synchronousStageExecutor).getReporter(applicationContext, apiKey)
        order.verify(reporterProxyStorage).getOrCreate(applicationContext, apiKey)
    }

    @Test
    fun activateReporter() {
        val apiKey = TestsData.generateApiKey()
        val config = ReporterConfig.newConfigBuilder(apiKey).withLogs().withSessionTimeout(15).build()
        val syncConfigCaptor = argumentCaptor<ReporterConfig>()
        val providerConfigCaptor = argumentCaptor<ReporterConfig>()
        proxy.activateReporter(context, config)
        val order = inOrder(barrier, synchronousStageExecutor, reporterProxyStorage)
        order.verify(barrier).activateReporter(same(context), syncConfigCaptor.capture())
        order.verify(synchronousStageExecutor).activateReporter(same(applicationContext), syncConfigCaptor.capture())
        order.verify(reporterProxyStorage).getOrCreate(same(applicationContext), providerConfigCaptor.capture())
        assertThat(syncConfigCaptor.firstValue).isEqualToComparingFieldByField(config)
        assertThat(providerConfigCaptor.firstValue).isEqualToComparingFieldByField(config)
    }

    @Test
    fun sendEventBuffer() {
        proxy.sendEventsBuffer()
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).sendEventsBuffer()
        order.verify(synchronousStageExecutor).sendEventsBuffer()
        order.verify(mainReporter).sendEventsBuffer()
    }

    @Test
    fun reportError() {
        val newThrowable: Throwable = mock()
        whenever(synchronousStageExecutor.reportError(ERROR_MESSAGE, throwable)).thenReturn(newThrowable)
        proxy.reportError(ERROR_MESSAGE, throwable)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportError(ERROR_MESSAGE, throwable)
        order.verify(synchronousStageExecutor).reportError(ERROR_MESSAGE, throwable)
        order.verify(mainReporter).reportError(ERROR_MESSAGE, newThrowable)
    }

    @Test
    fun reportCustomError() {
        val id = "ididid"
        proxy.reportError(id, ERROR_MESSAGE, throwable)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportError(id, ERROR_MESSAGE, throwable)
        order.verify(synchronousStageExecutor).reportError(id, ERROR_MESSAGE, throwable)
        order.verify(mainReporter).reportError(id, ERROR_MESSAGE, throwable)
    }

    @Test
    fun dataSendingEnabledIfTrue() = checkSetDataSendingEnabled(true)

    @Test
    fun dataSendingEnabledIfFalse() = checkSetDataSendingEnabled(false)

    @Test
    fun putErrorEnvironmentValue() {
        val key = "key"
        val value = "value"
        proxy.putErrorEnvironmentValue(key, value)
        val order = inOrder(barrier, synchronousStageExecutor, provider)
        order.verify(barrier).putErrorEnvironmentValue(key, value)
        order.verify(synchronousStageExecutor).putErrorEnvironmentValue(key, value)
        order.verify(provider).putErrorEnvironmentValue(key, value)
    }

    @Test
    fun initWebViewReporting() {
        val webView: WebView = mock()
        proxy.initWebViewReporting(webView)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).initWebViewReporting(webView)
        order.verify(synchronousStageExecutor).initWebViewReporting(webView, proxy)
        order.verify(mainReporter).onWebViewReportingInit(webViewJsInterfaceHandler)
    }

    @Test
    fun reportJsEvent() {
        val name = "My name"
        val value = "My value"
        whenever(barrier.reportJsEvent(name, value)).thenReturn(true)
        proxy.reportJsEvent(name, value)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportJsEvent(name, value)
        order.verify(synchronousStageExecutor).reportJsEvent(name, value)
        order.verify(mainReporter).reportJsEvent(name, value)
    }

    @Test
    fun reportJsEventInvalidParameters() {
        val name = "My name"
        val value = "My value"
        whenever(barrier.reportJsEvent(name, value)).thenReturn(false)
        proxy.reportJsEvent(name, value)
        inOrder(barrier).verify(barrier).reportJsEvent(name, value)
        verify(synchronousStageExecutor, never()).reportJsEvent(anyOrNull(), anyOrNull())
        verify(mainReporter, never()).reportJsEvent(anyOrNull(), anyOrNull())
    }

    @Test
    fun reportJsInitEventSuccessful() {
        val value = "My value"
        whenever(silentActivationValidator.validate()).thenReturn(ValidationResult.successful(mock<Validator<Any>>()))
        whenever(barrier.reportJsInitEvent(value)).thenReturn(true)
        proxy.reportJsInitEvent(value)
        val order = inOrder(silentActivationValidator, barrier, synchronousStageExecutor, mainReporter)
        order.verify(silentActivationValidator).validate()
        order.verify(barrier).reportJsInitEvent(value)
        order.verify(synchronousStageExecutor).reportJsInitEvent(value)
        order.verify(mainReporter).reportJsInitEvent(value)
    }

    @Test
    fun reportJsInitEventBarrierFails() {
        val value = "My value"
        whenever(silentActivationValidator.validate()).thenReturn(ValidationResult.successful(mock<Validator<Any>>()))
        whenever(barrier.reportJsInitEvent(value)).thenReturn(false)
        proxy.reportJsInitEvent(value)
        val order = inOrder(silentActivationValidator, barrier)
        order.verify(silentActivationValidator).validate()
        order.verify(barrier).reportJsInitEvent(value)
        verifyNoMoreInteractions(synchronousStageExecutor, mainReporter)
    }

    @Test
    fun reportJsInitEventNotActivated() {
        val value = "My value"
        whenever(silentActivationValidator.validate())
            .thenReturn(ValidationResult.failed(mock<Validator<Any>>(), "error"))
        whenever(barrier.reportJsInitEvent(value)).thenReturn(true)
        proxy.reportJsInitEvent(value)
        inOrder(silentActivationValidator).verify(silentActivationValidator).validate()
        verifyNoMoreInteractions(barrier, synchronousStageExecutor, mainReporter)
    }

    @Test
    fun getDeviceId() {
        val deviceID = "deviceID"
        whenever(ClientServiceLocator.getInstance().getStartupParams(applicationContext).deviceId).thenReturn(deviceID)
        assertThat(proxy.getDeviceId(context)).isEqualTo(deviceID)
        verify(barrier).getDeviceId(context)
        verify(synchronousStageExecutor).getDeviceId(applicationContext)
    }

    @Test
    fun getDeviceIdNull() {
        whenever(ClientServiceLocator.getInstance().getStartupParams(applicationContext).deviceId).thenReturn(null)
        assertThat(proxy.getDeviceId(context)).isNull()
        verify(barrier).getDeviceId(context)
        verify(synchronousStageExecutor).getDeviceId(applicationContext)
    }

    @Test
    fun getUuid() {
        val uuidResult: IdentifiersResult = mock()
        whenever(ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(applicationContext).readUuid())
            .thenReturn(uuidResult)
        assertThat(proxy.getUuid(context)).isEqualTo(uuidResult)
    }

    @Test
    fun putAppEnvironmentValue() {
        val key = "key"
        val value = "value"
        proxy.putAppEnvironmentValue(key, value)
        val order = inOrder(barrier, synchronousStageExecutor, provider)
        order.verify(barrier).putAppEnvironmentValue(key, value)
        order.verify(synchronousStageExecutor).putAppEnvironmentValue(key, value)
        order.verify(provider).putAppEnvironmentValue(key, value)
    }

    @Test
    fun clearAppEnvironment() {
        proxy.clearAppEnvironment()
        val order = inOrder(barrier, synchronousStageExecutor, provider)
        order.verify(barrier).clearAppEnvironment()
        order.verify(synchronousStageExecutor).clearAppEnvironment()
        order.verify(provider).clearAppEnvironment()
    }

    @Test
    fun clearErrorEnvironment() {
        proxy.clearErrorEnvironment()
        val order = inOrder(barrier, synchronousStageExecutor, provider)
        order.verify(barrier).clearErrorEnvironment()
        order.verify(synchronousStageExecutor).clearErrorEnvironment()
        order.verify(provider).clearErrorEnvironment()
    }

    @Test
    fun registerAnrListener() {
        val listener: AnrListener = mock()
        proxy.registerAnrListener(listener)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).registerAnrListener(eq(listener))
        order.verify(synchronousStageExecutor).registerAnrListener(eq(listener))
        order.verify(mainReporter).registerAnrListener(eq(listener))
    }

    @Test
    fun reportExternalAttribution() {
        val attribution: ExternalAttribution = mock()
        proxy.reportExternalAttribution(attribution)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportExternalAttribution(eq(attribution))
        order.verify(synchronousStageExecutor).reportExternalAttribution(eq(attribution))
        order.verify(mainReporter).reportExternalAttribution(eq(attribution))
    }

    @Test
    fun reportExternalAdRevenue() {
        val moduleAdRevenueProcessor: ModuleAdRevenueProcessor = mock()
        whenever(ClientServiceLocator.getInstance().modulesController.getModuleAdRevenueProcessor())
            .thenReturn(moduleAdRevenueProcessor)
        val value = "string"
        proxy.reportExternalAdRevenue(value)
        val order = inOrder(barrier, synchronousStageExecutor, moduleAdRevenueProcessor)
        order.verify(barrier).reportExternalAdRevenue(value)
        order.verify(synchronousStageExecutor).reportExternalAdRevenue(value)
        order.verify(moduleAdRevenueProcessor).process(value)
    }

    @Test
    fun reportAppMetricaEvent() {
        val event: AppMetricaEvent = mock()
        proxy.reportEvent(event)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportEvent(event)
        order.verify(synchronousStageExecutor).reportEvent(event)
        order.verify(mainReporter).reportEvent(event)
    }

    @Test
    fun reportAnr() {
        val thread: Thread = mock()
        val stackTraceElements = arrayOf(mock<StackTraceElement>())
        val allThreads = mapOf(thread to stackTraceElements)
        val allThreadsCaptor = argumentCaptor<Map<Thread, Array<StackTraceElement>>>()
        proxy.reportAnr(allThreads)
        val order = inOrder(barrier, synchronousStageExecutor, mainReporter)
        order.verify(barrier).reportAnr(allThreads)
        order.verify(synchronousStageExecutor).reportAnr(allThreads)
        order.verify(mainReporter).reportAnr(allThreadsCaptor.capture())
        order.verifyNoMoreInteractions()
        assertThat(allThreadsCaptor.firstValue)
            .isNotSameAs(allThreads)
            .containsExactlyEntriesOf(allThreads)
    }

    @Test
    fun warmUpForSelfProcess() {
        proxy.warmUpForSelfProcess(context)
        val order = inOrder(barrier, synchronousStageExecutor, provider, mainReporter)
        order.verify(barrier).warmUpForSelfProcess(context)
        order.verify(synchronousStageExecutor).warmUpForSelfReporter(context)
        order.verify(provider).getInitializedImpl(context)
        order.verifyNoMoreInteractions()
    }

    private fun checkSetDataSendingEnabled(value: Boolean) {
        proxy.setDataSendingEnabled(value)
        val order = inOrder(barrier, synchronousStageExecutor, provider)
        order.verify(barrier).setDataSendingEnabled(value)
        order.verify(synchronousStageExecutor).setDataSendingEnabled(value)
        order.verify(provider).setDataSendingEnabled(value)
    }

    private fun createProxy() = AppMetricaProxy(
        provider,
        barrier,
        silentActivationValidator,
        webViewJsInterfaceHandler,
        synchronousStageExecutor,
        reporterProxyStorage,
        defaultOneShotMetricaConfig,
        sessionsTrackingManager
    )

    private companion object {
        const val ERROR_MESSAGE = "error message"
    }
}
