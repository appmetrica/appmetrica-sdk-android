package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.StartupParamsItem
import io.appmetrica.analytics.internal.IdentifiersResult

internal class FeaturesHolder {

    private val startupParamItemAdapter = StartupParamItemAdapter()

    var features: FeaturesInternal = FeaturesInternal()
        @Synchronized get
        @Synchronized set

    @Synchronized
    fun putToMap(identifiers: List<String>, map: MutableMap<String, StartupParamsItem>) {
        identifiers.forEach { identifier ->
            when (identifier) {
                Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED ->
                    features.sslPinning?.let {
                        FeatureUtils.featureToIdentifierResultInternal(it, features.status, features.errorExplanation)
                    }?.let {
                        map[Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED] =
                            startupParamItemAdapter.adapt(it)
                    }
            }
        }
    }

    fun getFeature(key: String): IdentifiersResult? {
        with(features) {
            return when (key) {
                Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED ->
                    sslPinning?.let { FeatureUtils.featureToIdentifierResultInternal(it, status, errorExplanation) }
                else -> null
            }
        }
    }
}
