package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.IdentifiersResult
import io.appmetrica.analytics.StartupParamsItem
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus

private const val KEY_TRUE = "true"
private const val KEY_FALSE = "false"

internal object FeatureUtils {

    fun featureToIdentifierResultInternal(
        value: Boolean,
        status: IdentifierStatus,
        errorExplanation: String?
    ): IdentifiersResult {
        return IdentifiersResult(
            when (value) {
                true -> KEY_TRUE
                false -> KEY_FALSE
            },
            status,
            errorExplanation
        )
    }

    @JvmStatic
    fun identifierResultToFeature(result: IdentifiersResult?): Boolean? = result?.id?.let {
        when (it) {
            KEY_TRUE -> true
            KEY_FALSE -> false
            else -> null
        }
    }

    @JvmStatic
    fun startupParamsItemToFeature(result: StartupParamsItem?): Boolean? = result?.id?.let {
        when (it) {
            KEY_TRUE -> true
            KEY_FALSE -> false
            else -> null
        }
    }
}
