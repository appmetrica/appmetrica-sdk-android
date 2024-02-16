package io.appmetrica.analytics.impl.proxy.validation

import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.Revenue
import io.appmetrica.analytics.ecommerce.ECommerceEvent
import io.appmetrica.analytics.impl.crash.client.AllThreads
import io.appmetrica.analytics.impl.crash.client.UnhandledException
import io.appmetrica.analytics.impl.utils.validation.NonEmptyStringValidator
import io.appmetrica.analytics.impl.utils.validation.NonNullValidator
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator
import io.appmetrica.analytics.profile.UserProfile

class ReporterBarrier {

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

    fun reportEvent(eventName: String?) {
        eventNameValidator.validate(eventName)
    }

    fun reportEvent(
        eventName: String?,
        jsonValue: String?
    ) {
        eventNameValidator.validate(eventName)
    }

    fun reportEvent(
        eventName: String?,
        attributes: Map<String?, Any?>?
    ) {
        eventNameValidator.validate(eventName)
    }

    fun reportError(
        message: String?,
        error: Throwable?
    ) {
        errorMessageValidator.validate(message)
    }

    fun reportError(
        identifier: String?,
        message: String?,
        nonNullError: Throwable?
    ) {
        errorIdentifierValidator.validate(identifier)
    }

    fun reportUnhandledException(exception: Throwable?) {
        throwableExceptionValidator.validate(exception)
    }

    fun reportUnhandledException(exception: UnhandledException?) {
        unhandledExceptionValidator.validate(exception)
    }

    fun resumeSession() {}

    fun pauseSession() {}

    fun setUserProfileID(profileID: String?) {}

    fun reportUserProfile(profile: UserProfile?) {
        userProfileValidator.validate(profile)
    }

    fun reportRevenue(revenue: Revenue?) {
        revenueNonNullValidator.validate(revenue)
    }

    fun reportECommerce(event: ECommerceEvent?) {
        eCommerceNonNullValidator.validate(event)
    }

    fun setDataSendingEnabled(enabled: Boolean?) {}

    fun reportAdRevenue(adRevenue: AdRevenue?) {
        adRevenueNonNullValidator.validate(adRevenue)
    }

    fun putAppEnvironmentValue(
        key: String?,
        value: String?
    ) {}

    fun clearAppEnvironment() {}

    fun sendEventsBuffer() {}

    fun reportAnr(allThreads: AllThreads?) {}

    fun activate(config: ReporterConfig?) {}

    fun reportEvent(moduleEvent: ModuleEvent?) {}

    fun setSessionExtra(key: String?, value: ByteArray?) {}
}
