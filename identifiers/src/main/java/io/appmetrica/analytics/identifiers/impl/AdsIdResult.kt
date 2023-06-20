package io.appmetrica.analytics.identifiers.impl

import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus

internal fun getProviderUnavailableResult(errorMessage: String): AdsIdResult {
    return AdsIdResult(
        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE,
        null,
        errorMessage
    )
}

internal data class AdsIdResult(
    val status: IdentifierStatus,
    val adsIdInfo: AdsIdInfo? = null,
    val errorExplanation: String? = null,
) {

    fun toBundle(): Bundle = Bundle().apply {
        adsIdInfo?.toBundle()?.let {
            putBundle(Constants.TRACKING_INFO, it)
        }
        putString(Constants.STATUS, status.value)
        putString(Constants.ERROR_MESSAGE, errorExplanation)
    }
}
