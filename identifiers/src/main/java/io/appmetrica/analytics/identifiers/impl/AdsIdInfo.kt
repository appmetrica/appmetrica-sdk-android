package io.appmetrica.analytics.identifiers.impl

import android.os.Bundle

internal data class AdsIdInfo(
    val provider: String,
    val advId: String? = null,
    val limitedAdTracking: Boolean? = null
) {

    fun toBundle(): Bundle = Bundle().apply {
        putString(Constants.PROVIDER, provider)
        putString(Constants.ID, advId)
        limitedAdTracking?.let {
            putBoolean(Constants.LIMITED, it)
        }
    }
}
