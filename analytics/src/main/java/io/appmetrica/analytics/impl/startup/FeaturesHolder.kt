package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.IdentifiersResult
import io.appmetrica.analytics.impl.startup.FeatureUtils.featureToIdentifierResultInternal

internal class FeaturesHolder {

    var features: FeaturesInternal = FeaturesInternal()
        @Synchronized get
        @Synchronized set

    @Synchronized
    fun putToMap(identifiers: List<String>, map: MutableMap<String, IdentifiersResult>) {
        identifiers.forEach { identifier ->
            when (identifier) {
                Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED ->
                    features.sslPinning?.featureToIdentifierResultInternal(
                        features.status,
                        features.errorExplanation
                    )?.let {
                        map[Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED] = it
                    }
            }
        }
    }

    fun getFeature(key: String): IdentifiersResult? {
        with(features) {
            return when (key) {
                Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED ->
                    sslPinning?.featureToIdentifierResultInternal(status, errorExplanation)
                else -> null
            }
        }
    }
}
