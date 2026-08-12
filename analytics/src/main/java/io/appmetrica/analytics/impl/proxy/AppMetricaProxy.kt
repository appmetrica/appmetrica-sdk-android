package io.appmetrica.analytics.impl.proxy

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.webkit.WebView
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.AnrListener
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.DeferredDeeplinkListener
import io.appmetrica.analytics.DeferredDeeplinkParametersListener
import io.appmetrica.analytics.ExternalAttribution
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.Revenue
import io.appmetrica.analytics.StartupParamsCallback
import io.appmetrica.analytics.coreapi.event.AppMetricaEvent
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils
import io.appmetrica.analytics.ecommerce.ECommerceEvent
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig
import io.appmetrica.analytics.impl.IMainReporter
import io.appmetrica.analytics.impl.IReporterExtended
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.impl.SessionsTrackingManager
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler
import io.appmetrica.analytics.impl.proxy.synchronous.SynchronousStageExecutor
import io.appmetrica.analytics.impl.proxy.validation.Barrier
import io.appmetrica.analytics.impl.proxy.validation.SilentActivationValidator
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor
import io.appmetrica.analytics.profile.UserProfile

internal class AppMetricaProxy @VisibleForTesting internal constructor(
    provider: AppMetricaFacadeProvider,
    private val barrier: Barrier,
    private val silentActivationValidator: SilentActivationValidator,
    webViewJsInterfaceHandler: WebViewJsInterfaceHandler,
    private val synchronousStageExecutor: SynchronousStageExecutor,
    reporterProxyStorage: ReporterProxyStorage,
    defaultOneShotConfig: DefaultOneShotMetricaConfig,
    private val sessionsTrackingManager: SessionsTrackingManager
) : BaseAppMetricaProxy(
    provider,
    webViewJsInterfaceHandler,
    reporterProxyStorage,
    defaultOneShotConfig
) {

    constructor() : this(
        ClientServiceLocator.getInstance().appMetricaFacadeProvider,
        WebViewJsInterfaceHandler()
    )

    private constructor(
        provider: AppMetricaFacadeProvider,
        webViewJsInterfaceHandler: WebViewJsInterfaceHandler
    ) : this(
        provider,
        Barrier(provider),
        SilentActivationValidator(provider),
        webViewJsInterfaceHandler,
        SynchronousStageExecutor(provider, webViewJsInterfaceHandler),
        ReporterProxyStorage.getInstance(),
        ClientServiceLocator.getInstance().defaultOneShotConfig,
        ClientServiceLocator.getInstance().sessionsTrackingManager
    )

    private val mainReporter: IMainReporter
        get() = mainReporterApiConsumerProvider.mainReporter

    private val mainReporterApiConsumerProvider: MainReporterApiConsumerProvider
        get() = provider.peekInitializedImpl()!!.mainReporterApiConsumerProvider!!

    fun activate(context: Context, config: AppMetricaConfig) {
        barrier.activate(context, config)
        synchronousStageExecutor.activate(context.applicationContext, config)
        dispatch {
            provider.getInitializedImpl(context.applicationContext).activateFull(
                defaultOneShotConfig.mergeWithUserConfig(config)
            )
        }
        provider.markActivated()
    }

    fun sendEventsBuffer() {
        barrier.sendEventsBuffer()
        synchronousStageExecutor.sendEventsBuffer()
        dispatch { mainReporter.sendEventsBuffer() }
    }

    fun resumeSession(activity: Activity?) {
        barrier.resumeSession()
        synchronousStageExecutor.resumeSession(activity)
        dispatch { sessionsTrackingManager.resumeActivityManually(activity, mainReporter) }
    }

    fun pauseSession(activity: Activity?) {
        barrier.pauseSession()
        synchronousStageExecutor.pauseSession(activity)
        dispatch { sessionsTrackingManager.pauseActivityManually(activity, mainReporter) }
    }

    fun enableActivityAutoTracking(application: Application) {
        barrier.enableActivityAutoTracking(application)
        synchronousStageExecutor.enableActivityAutoTracking(application)
        dispatch {
            val status = sessionsTrackingManager.startWatchingIfNotYet()
            mainReporter.onEnableAutoTrackingAttemptOccurred(status)
        }
    }

    fun reportEvent(eventName: String) {
        barrier.reportEvent(eventName)
        synchronousStageExecutor.reportEvent(eventName)
        dispatch { mainReporter.reportEvent(eventName) }
    }

    fun reportEvent(eventName: String, jsonValue: String?) {
        barrier.reportEvent(eventName, jsonValue)
        synchronousStageExecutor.reportEvent(eventName, jsonValue)
        dispatch { mainReporter.reportEvent(eventName, jsonValue) }
    }

    fun reportEvent(eventName: String, attributes: Map<String, Any?>?) {
        barrier.reportEvent(eventName, attributes)
        synchronousStageExecutor.reportEvent(eventName, attributes)
        val entries = CollectionUtils.getListFromMap(attributes)
        dispatch { mainReporter.reportEvent(eventName, CollectionUtils.getMapFromList(entries)) }
    }

    fun reportError(message: String, error: Throwable?) {
        barrier.reportError(message, error)
        val nonNullError = synchronousStageExecutor.reportError(message, error)
        dispatch { mainReporter.reportError(message, nonNullError) }
    }

    fun reportError(identifier: String, message: String?, error: Throwable?) {
        barrier.reportError(identifier, message, error)
        synchronousStageExecutor.reportError(identifier, message, error)
        dispatch { mainReporter.reportError(identifier, message, error) }
    }

    fun reportUnhandledException(exception: Throwable) {
        barrier.reportUnhandledException(exception)
        synchronousStageExecutor.reportUnhandledException(exception)
        dispatch { mainReporter.reportUnhandledException(exception) }
    }

    fun reportAppOpen(activity: Activity) {
        barrier.reportAppOpen(activity)
        val openIntent = synchronousStageExecutor.reportAppOpen(activity)
        dispatch { mainReporterApiConsumerProvider.deeplinkConsumer.reportAppOpen(openIntent) }
    }

    fun reportAppOpen(deeplink: String) {
        barrier.reportAppOpen(deeplink)
        synchronousStageExecutor.reportAppOpen(deeplink)
        dispatch { mainReporterApiConsumerProvider.deeplinkConsumer.reportAppOpen(deeplink) }
    }

    fun reportAppOpen(intent: Intent) {
        barrier.reportAppOpen(intent)
        synchronousStageExecutor.reportAppOpen(intent)
        dispatch { mainReporterApiConsumerProvider.deeplinkConsumer.reportAppOpen(intent) }
    }

    fun setLocation(location: Location?) {
        barrier.setLocation(location)
        synchronousStageExecutor.setLocation(location)
        dispatch { provider.setLocation(location) }
    }

    fun setLocationTracking(enabled: Boolean) {
        barrier.setLocationTracking(enabled)
        synchronousStageExecutor.setLocationTracking(enabled)
        dispatch { provider.setLocationTracking(enabled) }
    }

    fun setAdvIdentifiersTracking(enabled: Boolean) {
        barrier.setAdvIdentifiersTracking(enabled)
        synchronousStageExecutor.setAdvIdentifiersTracking(enabled)
        dispatch { provider.setAdvIdentifiersTracking(enabled) }
    }

    fun setDataSendingEnabled(enabled: Boolean) {
        barrier.setDataSendingEnabled(enabled)
        synchronousStageExecutor.setDataSendingEnabled(enabled)
        dispatch { provider.setDataSendingEnabled(enabled) }
    }

    fun setUserProfileID(userProfileID: String?) {
        barrier.setUserProfileID(userProfileID)
        synchronousStageExecutor.setUserProfileID(userProfileID)
        dispatch { provider.setUserProfileID(userProfileID) }
    }

    fun reportUserProfile(profile: UserProfile) {
        barrier.reportUserProfile(profile)
        synchronousStageExecutor.reportUserProfile(profile)
        dispatch { mainReporter.reportUserProfile(profile) }
    }

    fun reportRevenue(revenue: Revenue) {
        barrier.reportRevenue(revenue)
        synchronousStageExecutor.reportRevenue(revenue)
        dispatch { mainReporter.reportRevenue(revenue) }
    }

    fun reportAdRevenue(adRevenue: AdRevenue) {
        barrier.reportAdRevenue(adRevenue)
        synchronousStageExecutor.reportAdRevenue(adRevenue)
        dispatch { mainReporter.reportAdRevenue(adRevenue) }
    }

    fun reportECommerce(event: ECommerceEvent) {
        barrier.reportECommerce(event)
        synchronousStageExecutor.reportECommerce(event)
        dispatch { mainReporter.reportECommerce(event) }
    }

    fun requestDeferredDeeplinkParameters(listener: DeferredDeeplinkParametersListener) {
        barrier.requestDeferredDeeplinkParameters(listener)
        synchronousStageExecutor.requestDeferredDeeplinkParameters(listener)
        dispatch { provider.peekInitializedImpl()!!.requestDeferredDeeplinkParameters(listener) }
    }

    fun requestDeferredDeeplink(listener: DeferredDeeplinkListener) {
        barrier.requestDeferredDeeplink(listener)
        synchronousStageExecutor.requestDeferredDeeplink(listener)
        dispatch { provider.peekInitializedImpl()!!.requestDeferredDeeplink(listener) }
    }

    fun getReporter(context: Context, apiKey: String): IReporterExtended {
        barrier.getReporter(context, apiKey)
        synchronousStageExecutor.getReporter(context.applicationContext, apiKey)
        return reporterProxyStorage.getOrCreate(context.applicationContext, apiKey)
    }

    fun activateReporter(context: Context, config: ReporterConfig) {
        DebugLogger.info("[AppMetricaProxy]", "activate reporter with apiKey = ${config.apiKey}")
        barrier.activateReporter(context, config)
        synchronousStageExecutor.activateReporter(context.applicationContext, config)
        reporterProxyStorage.getOrCreate(context.applicationContext, config)
    }

    fun putErrorEnvironmentValue(key: String, value: String?) {
        barrier.putErrorEnvironmentValue(key, value)
        synchronousStageExecutor.putErrorEnvironmentValue(key, value)
        dispatch { provider.putErrorEnvironmentValue(key, value) }
    }

    fun initWebViewReporting(webView: WebView) {
        barrier.initWebViewReporting(webView)
        synchronousStageExecutor.initWebViewReporting(webView, this)
        dispatch { mainReporter.onWebViewReportingInit(webViewJsInterfaceHandler) }
    }

    fun reportJsEvent(eventName: String, eventValue: String?) {
        if (!barrier.reportJsEvent(eventName, eventValue)) {
            DebugLogger.warning(
                SdkUtils.APPMETRICA_TAG,
                "Impossible to report event because parameters are invalid."
            )
            return
        }
        synchronousStageExecutor.reportJsEvent(eventName, eventValue)
        dispatch { mainReporter.reportJsEvent(eventName, eventValue) }
    }

    fun reportJsInitEvent(value: String) {
        if (!silentActivationValidator.validate().isValid) {
            DebugLogger.warning(
                "[AppMetricaProxy]",
                "Impossible to report JS init event because AppMetrica has not been activated yet"
            )
            return
        }
        if (!barrier.reportJsInitEvent(value)) {
            DebugLogger.warning(
                "[AppMetricaProxy]",
                "Impossible to report JS init event because value is invalid"
            )
            return
        }
        synchronousStageExecutor.reportJsInitEvent(value)
        dispatch { mainReporter.reportJsInitEvent(value) }
    }

    fun getDeviceId(context: Context): String? {
        barrier.getDeviceId(context)
        synchronousStageExecutor.getDeviceId(context.applicationContext)
        return ClientServiceLocator.getInstance().getStartupParams(context.applicationContext).deviceId
    }

    fun getUuid(context: Context): IdentifiersResult {
        barrier.getUuid(context)
        synchronousStageExecutor.getUuid(context.applicationContext)
        return ClientServiceLocator.getInstance()
            .getMultiProcessSafeUuidProvider(context.applicationContext)
            .readUuid()
    }

    fun putAppEnvironmentValue(key: String, value: String?) {
        barrier.putAppEnvironmentValue(key, value)
        synchronousStageExecutor.putAppEnvironmentValue(key, value)
        dispatch { provider.putAppEnvironmentValue(key, value) }
    }

    fun clearAppEnvironment() {
        barrier.clearAppEnvironment()
        synchronousStageExecutor.clearAppEnvironment()
        dispatch { provider.clearAppEnvironment() }
    }

    fun clearErrorEnvironment() {
        barrier.clearErrorEnvironment()
        synchronousStageExecutor.clearErrorEnvironment()
        dispatch { provider.clearErrorEnvironment() }
    }

    fun requestStartupParams(
        context: Context,
        callback: StartupParamsCallback,
        params: List<String>
    ) {
        DebugLogger.info("[AppMetricaProxy]", "requestStartupParams for keys: $params")
        barrier.requestStartupParams(context, callback, params)
        synchronousStageExecutor.requestStartupParams(context.applicationContext, callback, params)
        dispatch {
            provider.getInitializedImpl(context.applicationContext).requestStartupParams(callback, params)
        }
    }

    fun registerAnrListener(listener: AnrListener) {
        barrier.registerAnrListener(listener)
        synchronousStageExecutor.registerAnrListener(listener)
        dispatch { mainReporter.registerAnrListener(listener) }
    }

    fun reportExternalAttribution(value: ExternalAttribution) {
        barrier.reportExternalAttribution(value)
        synchronousStageExecutor.reportExternalAttribution(value)
        dispatch { mainReporter.reportExternalAttribution(value) }
    }

    fun reportExternalAdRevenue(vararg values: Any) {
        barrier.reportExternalAdRevenue(*values)
        synchronousStageExecutor.reportExternalAdRevenue(*values)
        dispatch {
            val processor: ModuleAdRevenueProcessor? =
                ClientServiceLocator.getInstance().modulesController.getModuleAdRevenueProcessor()
            processor?.process(*values)
        }
    }

    fun reportAnr(allThreads: Map<Thread, Array<StackTraceElement>>) {
        barrier.reportAnr(allThreads)
        synchronousStageExecutor.reportAnr(allThreads)
        val entries = CollectionUtils.getListFromMap(allThreads)
        dispatch { mainReporter.reportAnr(CollectionUtils.getMapFromList(entries)) }
    }

    fun warmUpForSelfProcess(context: Context) {
        barrier.warmUpForSelfProcess(context)
        synchronousStageExecutor.warmUpForSelfReporter(context)
        provider.getInitializedImpl(context)
    }

    fun reportEvent(event: AppMetricaEvent) {
        barrier.reportEvent(event)
        synchronousStageExecutor.reportEvent(event)
        dispatch { mainReporter.reportEvent(event) }
    }

    @VisibleForTesting
    protected fun getMainFacadeBarrier(): Barrier = barrier
}
