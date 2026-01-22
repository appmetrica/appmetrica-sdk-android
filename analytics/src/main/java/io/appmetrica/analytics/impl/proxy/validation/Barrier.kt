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
import io.appmetrica.analytics.ecommerce.ECommerceEvent
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.impl.utils.validation.NonEmptyStringValidator
import io.appmetrica.analytics.impl.utils.validation.NonNullValidator
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator
import io.appmetrica.analytics.impl.utils.validation.api.ApiKeyValidator
import io.appmetrica.analytics.profile.UserProfile

internal class Barrier(
    provider: AppMetricaFacadeProvider
) {

    private val activationValidator = ActivationValidator(provider)
    private val configValidator = ThrowIfFailedValidator(
        NonNullValidator<AppMetricaConfig>("Config")
    )
    private val activityValidator = ThrowIfFailedValidator(
        NonNullValidator<Activity>("Activity")
    )
    private val intentValidator = ThrowIfFailedValidator(
        NonNullValidator<Intent>("Intent")
    )
    private val applicationValidator = ThrowIfFailedValidator(
        NonNullValidator<Application>("Application")
    )
    private val contextValidator = ThrowIfFailedValidator(
        NonNullValidator<Context>("Context")
    )
    private val deeplinkListenerValidator = ThrowIfFailedValidator(
        NonNullValidator<Any>("Deeplink listener")
    )
    private val reporterConfigValidator = ThrowIfFailedValidator(
        NonNullValidator<ReporterConfig>("Reporter Config")
    )
    private val deeplinkValidator = ThrowIfFailedValidator(
        NonEmptyStringValidator("Deeplink")
    )
    private val apiKeyValidator = ThrowIfFailedValidator(ApiKeyValidator())
    private val nonNullKeyValidator = ThrowIfFailedValidator(
        NonNullValidator<String>("Key")
    )
    private val nonNullWebViewValidator = ThrowIfFailedValidator(
        NonNullValidator<WebView>("WebView")
    )
    private val silentNonEmptyValueValidator = NonEmptyStringValidator("value")
    private val silentNonEmptyNameValidator = NonEmptyStringValidator("name")
    private val callbackValidator = ThrowIfFailedValidator(
        NonNullValidator<Any>("AppMetricaDeviceIdentifiers callback")
    )
    private val anrListenerValidator = ThrowIfFailedValidator(
        NonNullValidator<AnrListener>("ANR listener")
    )
    private val externalAttributionValidator = ThrowIfFailedValidator(
        NonNullValidator<ExternalAttribution>("External attribution")
    )
    private val eventNameValidator = ThrowIfFailedValidator(
        NonEmptyStringValidator("Event name")
    )
    private val errorMessageValidator = ThrowIfFailedValidator(
        NonEmptyStringValidator("Error message")
    )
    private val errorIdentifierValidator = ThrowIfFailedValidator(
        NonEmptyStringValidator("Error identifier")
    )
    private val unhandledExceptionValidator = ThrowIfFailedValidator(
        NonNullValidator<UnhandledException>("Unhandled exception")
    )
    private val throwableExceptionValidator = ThrowIfFailedValidator(
        NonNullValidator<Throwable>("Throwable")
    )
    private val userProfileValidator = ThrowIfFailedValidator(
        NonNullValidator<UserProfile>("User profile")
    )
    private val revenueNonNullValidator = ThrowIfFailedValidator(
        NonNullValidator<Revenue>("Revenue")
    )
    private val adRevenueNonNullValidator = ThrowIfFailedValidator(
        NonNullValidator<AdRevenue>("AdRevenue")
    )
    private val eCommerceNonNullValidator = ThrowIfFailedValidator(
        NonNullValidator<ECommerceEvent>("ECommerceEvent")
    )

    private val anrAllThreadValidator = ThrowIfFailedValidator(
        NonNullValidator<Map<Thread, Array<StackTraceElement>>>("Anr all threads")
    )

    fun enableActivityAutoTracking(application: Application?) {
        applicationValidator.validate(application)
    }

    fun reportAppOpen(activity: Activity?) {
        activationValidator.validate()
        activityValidator.validate(activity)
    }

    fun reportAppOpen(deeplink: String?) {
        activationValidator.validate()
        deeplinkValidator.validate(deeplink)
    }

    fun reportAppOpen(intent: Intent?) {
        activationValidator.validate()
        intentValidator.validate(intent)
    }

    fun setLocation(location: Location?) {}

    fun setLocationTracking(enabled: Boolean) {}

    fun setAdvIdentifiersTracking(enabled: Boolean) {}

    fun requestDeferredDeeplinkParameters(listener: DeferredDeeplinkParametersListener?) {
        activationValidator.validate()
        deeplinkListenerValidator.validate(listener)
    }

    fun requestDeferredDeeplink(listener: DeferredDeeplinkListener?) {
        activationValidator.validate()
        deeplinkListenerValidator.validate(listener)
    }

    fun setDataSendingEnabled(enabled: Boolean) {}

    fun getReporter(
        context: Context?,
        apiKey: String?
    ) {
        contextValidator.validate(context)
        apiKeyValidator.validate(apiKey)
    }

    fun activateReporter(
        context: Context?,
        config: ReporterConfig?
    ) {
        contextValidator.validate(context)
        reporterConfigValidator.validate(config)
    }

    fun activate(
        context: Context?,
        config: AppMetricaConfig?
    ) {
        contextValidator.validate(context)
        configValidator.validate(config)
    }

    fun putErrorEnvironmentValue(
        key: String?,
        value: String?
    ) {
        nonNullKeyValidator.validate(key)
    }

    fun initWebViewReporting(webView: WebView?) {
        activationValidator.validate()
        nonNullWebViewValidator.validate(webView)
    }

    fun reportJsEvent(
        eventName: String?,
        eventValue: String?
    ): Boolean {
        activationValidator.validate()
        return silentNonEmptyNameValidator.validate(eventName).isValid
    }

    fun reportJsInitEvent(value: String?): Boolean {
        return silentNonEmptyValueValidator.validate(value).isValid
    }

    fun requestStartupParams(
        context: Context?,
        callback: StartupParamsCallback?,
        params: List<String?>?
    ) {
        contextValidator.validate(context)
        callbackValidator.validate(callback)
    }

    fun getUuid(context: Context?) {
        contextValidator.validate(context)
    }

    fun getDeviceId(context: Context?) {
        contextValidator.validate(context)
    }

    fun registerAnrListener(listener: AnrListener?) {
        activationValidator.validate()
        anrListenerValidator.validate(listener)
    }

    fun reportExternalAttribution(value: ExternalAttribution?) {
        activationValidator.validate()
        externalAttributionValidator.validate(value)
    }

    fun reportEvent(eventName: String?) {
        activationValidator.validate()
        eventNameValidator.validate(eventName)
    }

    fun reportEvent(
        eventName: String?,
        jsonValue: String?
    ) {
        activationValidator.validate()
        eventNameValidator.validate(eventName)
    }

    fun reportEvent(
        eventName: String?,
        attributes: Map<String?, Any?>?
    ) {
        activationValidator.validate()
        eventNameValidator.validate(eventName)
    }

    fun reportError(
        message: String?,
        error: Throwable?
    ) {
        activationValidator.validate()
        errorMessageValidator.validate(message)
    }

    fun reportError(
        identifier: String?,
        message: String?,
        nonNullError: Throwable?
    ) {
        activationValidator.validate()
        errorIdentifierValidator.validate(identifier)
    }

    fun reportUnhandledException(exception: Throwable?) {
        activationValidator.validate()
        throwableExceptionValidator.validate(exception)
    }

    fun resumeSession() {
        activationValidator.validate()
    }

    fun pauseSession() {
        activationValidator.validate()
    }

    fun setUserProfileID(profileID: String?) {}

    fun reportUserProfile(profile: UserProfile?) {
        activationValidator.validate()
        userProfileValidator.validate(profile)
    }

    fun reportRevenue(revenue: Revenue?) {
        activationValidator.validate()
        revenueNonNullValidator.validate(revenue)
    }

    fun reportECommerce(event: ECommerceEvent?) {
        activationValidator.validate()
        eCommerceNonNullValidator.validate(event)
    }

    fun reportAdRevenue(adRevenue: AdRevenue?) {
        activationValidator.validate()
        adRevenueNonNullValidator.validate(adRevenue)
    }

    fun putAppEnvironmentValue(
        key: String?,
        value: String?
    ) {}

    fun clearAppEnvironment() {}

    fun sendEventsBuffer() {
        activationValidator.validate()
    }

    fun reportExternalAdRevenue(vararg values: Any) {
        activationValidator.validate()
    }

    fun reportAnr(allThread: Map<Thread, Array<StackTraceElement>>?) {
        anrAllThreadValidator.validate(allThread)
    }

    fun warmUpForSelfProcess(context: Context?) {
        contextValidator.validate(context)
    }
}
