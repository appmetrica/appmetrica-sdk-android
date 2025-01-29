package io.appmetrica.analytics.impl.proxy.synchronous

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
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable
import io.appmetrica.analytics.coreutils.internal.WrapUtils
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils.accessSystemServiceSafely
import io.appmetrica.analytics.ecommerce.ECommerceEvent
import io.appmetrica.analytics.impl.ActivityLifecycleManager
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.ContextAppearedListener
import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.SessionsTrackingManager
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler
import io.appmetrica.analytics.impl.crash.AppMetricaThrowable
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy
import io.appmetrica.analytics.impl.utils.FirstLaunchDetector
import io.appmetrica.analytics.profile.UserProfile

class SynchronousStageExecutor @VisibleForTesting constructor(
    private val provider: AppMetricaFacadeProvider,
    private val webViewJsInterfaceHandler: WebViewJsInterfaceHandler,
    private val activityLifecycleManager: ActivityLifecycleManager,
    private val sessionsTrackingManager: SessionsTrackingManager,
    private val contextAppearedListener: ContextAppearedListener,
    private val firstLaunchDetector: FirstLaunchDetector
) {

    constructor(
        provider: AppMetricaFacadeProvider,
        webViewJsInterfaceHandler: WebViewJsInterfaceHandler
    ) : this(
        provider,
        webViewJsInterfaceHandler,
        ClientServiceLocator.getInstance().activityLifecycleManager,
        ClientServiceLocator.getInstance().sessionsTrackingManager,
        ClientServiceLocator.getInstance().contextAppearedListener,
        ClientServiceLocator.getInstance().firstLaunchDetector
    )

    fun putAppEnvironmentValue(
        key: String,
        value: String?
    ) {
    }

    fun clearAppEnvironment() {}

    fun sendEventsBuffer() {}

    fun reportEvent(eventName: String) {}

    fun reportEvent(
        eventName: String,
        jsonValue: String?
    ) {
    }

    fun reportEvent(
        eventName: String,
        attributes: Map<String?, Any?>?
    ) {
    }

    fun reportError(
        message: String,
        error: Throwable?
    ): Throwable {
        return error ?: AppMetricaThrowable().apply {
            fillInStackTrace()
        }
    }

    fun reportError(identifier: String, message: String?, error: Throwable?) {}

    fun reportUnhandledException(exception: Throwable) {}

    fun resumeSession(activity: Activity?) {}

    fun pauseSession(activity: Activity?) {}

    fun setUserProfileID(profileID: String?) {}

    fun reportUserProfile(profile: UserProfile) {}

    fun reportRevenue(revenue: Revenue) {}

    fun reportAdRevenue(adRevenue: AdRevenue) {}

    fun reportECommerce(event: ECommerceEvent) {}

    fun activate(
        context: Context,
        config: AppMetricaConfig
    ) {
        contextAppearedListener.onProbablyAppeared(context)
        val logger = LoggerStorage.getOrCreatePublicLogger(config.apiKey)
        val sessionsAutoTrackingEnabled =
            WrapUtils.getOrDefault(
                config.sessionsAutoTrackingEnabled,
                DefaultValues.DEFAULT_SESSIONS_AUTO_TRACKING_ENABLED
            )
        if (sessionsAutoTrackingEnabled) {
            logger.info("Session auto tracking enabled")
            sessionsTrackingManager.startWatchingIfNotYet()
        } else {
            logger.info("Session auto tracking disabled")
        }
        provider.getInitializedImpl(context).activateCore(config)
    }

    fun enableActivityAutoTracking(application: Application) {
        activityLifecycleManager.maybeInit(application)
    }

    fun reportAppOpen(activity: Activity?): Intent? {
        return accessSystemServiceSafely<Activity, Intent>(
            activity,
            "getting intent",
            "activity",
            FunctionWithThrowable { input -> input.intent }
        )
    }

    fun reportAppOpen(deeplink: String) {}

    fun reportAppOpen(intent: Intent) {}

    fun reportReferralUrl(referralUrl: String) {}

    fun setLocation(location: Location?) {}

    fun setLocationTracking(enabled: Boolean) {}

    fun setAdvIdentifiersTracking(enabled: Boolean) {}

    fun setDataSendingEnabled(enabled: Boolean) {}

    fun requestDeferredDeeplinkParameters(listener: DeferredDeeplinkParametersListener) {}

    fun requestDeferredDeeplink(listener: DeferredDeeplinkListener) {}

    fun getReporter(
        context: Context,
        apiKey: String
    ) {
        contextAppearedListener.onProbablyAppeared(context)
    }

    fun putErrorEnvironmentValue(
        key: String,
        value: String?
    ) {
    }

    fun activateReporter(
        context: Context,
        config: ReporterConfig
    ) {
        contextAppearedListener.onProbablyAppeared(context)
    }

    fun requestStartupParams(
        context: Context,
        callback: StartupParamsCallback,
        params: List<String?>
    ) {
        contextAppearedListener.onProbablyAppeared(context)
    }

    fun initWebViewReporting(
        webView: WebView,
        proxy: AppMetricaProxy
    ) {
        webViewJsInterfaceHandler.initWebViewReporting(webView, proxy)
    }

    fun reportJsEvent(
        eventName: String,
        eventValue: String?
    ) {
    }

    fun reportJsInitEvent(value: String) {}

    fun getUuid(context: Context) {
        contextAppearedListener.onProbablyAppeared(context)
        firstLaunchDetector.init(context)
    }

    fun registerAnrListener(listener: AnrListener) {}

    fun reportExternalAttribution(value: ExternalAttribution) {}

    fun reportExternalAdRevenue(vararg values: Any) {}

    fun reportAnr(allThreads: Map<Thread, Array<StackTraceElement>>) {}

    fun warmUpForSelfReporter(context: Context) {
        contextAppearedListener.onProbablyAppeared(context)
    }
}
