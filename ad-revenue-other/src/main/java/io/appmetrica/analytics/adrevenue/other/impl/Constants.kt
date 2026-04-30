package io.appmetrica.analytics.adrevenue.other.impl

internal object Constants {
    const val MODULE_ID = "ad-revenue-other"

    object Defaults {
        private val defaultConfig = AdRevenueOtherConfigProto()
        val DEFAULT_ENABLED = defaultConfig.enabled
        val DEFAULT_INCLUDE_SOURCE = defaultConfig.includeSource
    }

    object RemoteConfig {
        const val FEATURE_NAME = "ad_revenue_other"
        const val FEATURE_NAME_OBFUSCATED = "aro"
        const val INCLUDE_SOURCE_NAME = "ad_revenue_other_include_source"
        const val INCLUDE_SOURCE_NAME_OBFUSCATED = "arois"
    }

    object ServiceConfig {
        const val ENABLED = "enabled"
        const val INCLUDE_SOURCE = "include_source"
    }
}
