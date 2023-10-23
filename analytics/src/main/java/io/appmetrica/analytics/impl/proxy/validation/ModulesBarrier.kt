package io.appmetrica.analytics.impl.proxy.validation

import android.content.Context
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.impl.proxy.ActivationValidator
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider
import io.appmetrica.analytics.impl.utils.validation.NonNullValidator
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator
import io.appmetrica.analytics.impl.utils.validation.api.ApiKeyValidator

class ModulesBarrier(
    provider: AppMetricaFacadeProvider,
) {

    private val activationValidator = ActivationValidator(provider)
    private val contextValidator = ThrowIfFailedValidator(
        NonNullValidator<Context>("Context")
    )
    private val sessionExtraKeyValidator = ThrowIfFailedValidator(
        NonNullValidator<String>("Session extra key")
    )
    private val apiKeyValidator = ThrowIfFailedValidator(ApiKeyValidator())

    @Suppress("UNUSED_PARAMETER")
    fun reportEvent(
        event: ModuleEvent
    ) {
        activationValidator.validate()
    }

    @Suppress("UNUSED_PARAMETER")
    fun setSessionExtra(key: String?, value: ByteArray?) {
        sessionExtraKeyValidator.validate(key)
    }

    fun isActivatedForApp() {
    }

    fun sendEventsBuffer() {
    }

    fun getReporter(context: Context, apiKey: String) {
        contextValidator.validate(context)
        apiKeyValidator.validate(apiKey)
    }
}
