package io.appmetrica.analytics.impl.id.reflection

import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdProviderException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceAccessDeniedException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceBindingException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceCommunicationException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceConnectionTimeoutException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceNotFoundException
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceResponseException
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import java.lang.reflect.InvocationTargetException

internal object AdvIdProviderExceptionMapper {

    data class Failure(
        val result: AdTrackingInfoResult,
        val isRetryable: Boolean,
    )

    fun map(throwable: Throwable): Failure {
        val exception = (throwable as? InvocationTargetException)?.targetException ?: throwable
        return when (exception) {
            is ClassNotFoundException -> unavailable("identifier provider module not found")
            is AdvIdServiceNotFoundException -> unavailable("service not found")
            is AdvIdServiceBindingException -> unavailable("failed to bind service", true)
            is AdvIdServiceConnectionTimeoutException -> unavailable("service connection timed out", true)
            is AdvIdServiceCommunicationException -> unavailable("service communication failed", true)
            is AdvIdServiceAccessDeniedException -> unavailable("service access denied")
            is AdvIdServiceResponseException -> unavailable("service returned invalid binder")
            is AdvIdProviderException -> unavailable("identifier provider failed")
            else -> Failure(
                AdTrackingInfoResult(
                    null,
                    IdentifierStatus.UNKNOWN,
                    "identifier provider invocation failed (${exception.javaClass.simpleName})"
                ),
                false
            )
        }
    }

    private fun unavailable(details: String, isRetryable: Boolean = false): Failure {
        return Failure(
            AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, details),
            isRetryable
        )
    }
}
