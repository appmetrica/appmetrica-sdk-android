package io.appmetrica.analytics.impl.proxy.validation

import io.appmetrica.analytics.impl.utils.validation.NonEmptyCollectionValidator
import io.appmetrica.analytics.impl.utils.validation.NonEmptyStringValidator
import io.appmetrica.analytics.impl.utils.validation.NonNullValidator
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator
import io.appmetrica.analytics.plugins.PluginErrorDetails
import io.appmetrica.analytics.plugins.StackTraceItem

class PluginsReporterBarrier {

    private val pluginErrorDetailsNonNullValidator = ThrowIfFailedValidator(
        NonNullValidator<PluginErrorDetails>("Error details")
    )
    private val errorIdentifierValidator = ThrowIfFailedValidator(
        NonEmptyStringValidator("Error identifier")
    )
    private val silentNonEmptyStacktraceValidator =
        NonEmptyCollectionValidator<StackTraceItem>("Stacktrace")

    fun reportUnhandledException(errorDetails: PluginErrorDetails?) {
        pluginErrorDetailsNonNullValidator.validate(errorDetails)
    }

    fun reportError(
        errorDetails: PluginErrorDetails?,
        message: String?
    ) {
        pluginErrorDetailsNonNullValidator.validate(errorDetails)
    }

    fun reportErrorWithFilledStacktrace(
        errorDetails: PluginErrorDetails?,
        message: String?
    ): Boolean {
        reportError(errorDetails, message)
        return silentNonEmptyStacktraceValidator.validate(errorDetails?.stacktrace).isValid
    }

    fun reportError(
        identifier: String?,
        message: String?,
        errorDetails: PluginErrorDetails?
    ) {
        errorIdentifierValidator.validate(identifier)
    }
}
