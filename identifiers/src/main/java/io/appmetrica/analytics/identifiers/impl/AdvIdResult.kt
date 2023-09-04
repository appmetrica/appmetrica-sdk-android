package io.appmetrica.analytics.identifiers.impl

import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus

internal fun getProviderUnavailableResult(errorMessage: String): AdvIdResult {
    return AdvIdResult(
        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
        null,
        errorMessage
    )
}

internal data class AdvIdResult(
    val status: IdentifierStatus,
    val advIdInfo: AdvIdInfo? = null,
    val errorExplanation: String? = null,
) {

    fun toBundle(): Bundle = Bundle().apply {
        advIdInfo?.toBundle()?.let {
            putBundle(Constants.TRACKING_INFO, it)
        }
        putString(Constants.STATUS, status.value)
        putString(Constants.ERROR_MESSAGE, errorExplanation)
    }
}
