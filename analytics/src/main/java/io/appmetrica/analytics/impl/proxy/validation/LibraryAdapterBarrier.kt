package io.appmetrica.analytics.impl.proxy.validation

import android.content.Context
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.impl.utils.validation.NonNullValidator
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator

class LibraryAdapterBarrier(
    provider: AppMetricaFacadeProvider,
) {

    private val contextValidator = ThrowIfFailedValidator(
        NonNullValidator<Context>("Context")
    )
    private val activationValidator = ActivationValidator(provider)
    private val senderValidator = ThrowIfFailedValidator(
        NonNullValidator<String>("Sender")
    )
    private val eventValidator = ThrowIfFailedValidator(
        NonNullValidator<String>("Event")
    )
    private val payloadValidator = ThrowIfFailedValidator(
        NonNullValidator<String>("Payload")
    )

    fun activate(context: Context?) {
        contextValidator.validate(context)
    }

    fun reportEvent(
        sender: String?,
        event: String?,
        payload: String?
    ) {
        activationValidator.validate()
        senderValidator.validate(sender)
        eventValidator.validate(event)
        payloadValidator.validate(payload)
    }
}
