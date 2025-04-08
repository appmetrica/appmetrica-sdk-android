package io.appmetrica.analytics.impl.proxy.validation

import android.content.Context
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.impl.utils.validation.NonNullValidator

class LibraryAdapterBarrier(
    provider: AppMetricaFacadeProvider,
) {

    private val contextValidator = NonNullValidator<Context>("Context")
    private val configValidator = NonNullValidator<AppMetricaLibraryAdapterConfig>("Config")
    private val activationValidator = SilentActivationValidator(provider)
    private val senderValidator = NonNullValidator<String>("Sender")
    private val eventValidator = NonNullValidator<String>("Event")
    private val payloadValidator = NonNullValidator<String>("Payload")

    fun activate(context: Context?): Boolean = contextValidator.validate(context).isValid

    fun activate(
        context: Context?,
        appMetricaLibraryAdapterConfig: AppMetricaLibraryAdapterConfig?
    ): Boolean = contextValidator.validate(context).isValid &&
        configValidator.validate(appMetricaLibraryAdapterConfig).isValid

    fun setAdvIdentifiersTracking(enabled: Boolean): Boolean {
        return activationValidator.validate().isValid
    }

    fun reportEvent(
        sender: String?,
        event: String?,
        payload: String?
    ): Boolean {
        return activationValidator.validate().isValid &&
            senderValidator.validate(sender).isValid &&
            eventValidator.validate(event).isValid &&
            payloadValidator.validate(payload).isValid
    }
}
