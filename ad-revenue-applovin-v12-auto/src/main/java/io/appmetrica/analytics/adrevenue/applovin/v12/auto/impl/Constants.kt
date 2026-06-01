package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.protobuf.client.AdRevenueApplovinConfigProtobuf

internal object Constants {
    const val MODULE_ID = "ad-revenue-applovin-v12-auto"
    const val LIBRARY_COMMUNICATOR_CLASS = "com.applovin.communicator.AppLovinCommunicator"
    const val LIBRARY_MESSAGE_CLASS = "com.applovin.communicator.AppLovinCommunicatorMessage"
    const val AD_REVENUE_SOURCE_IDENTIFIER = "applovin"
    const val COMMUNICATOR_ID = "AppMetrica"
    const val TOPIC = "max_revenue_events"
    // AppLovin stores up to 10 messages per topic in a ring buffer and delivers them
    // to a new subscriber upon subscription (sticky messages). The cache size matches
    // this limit to deduplicate repeated delivery when resubscribing.
    const val DEDUPLICATION_CACHE_SIZE = 10

    object RemoteConfig {
        const val FEATURE_NAME = "ad_revenue_applovin_max"
        const val FEATURE_NAME_OBFUSCATED = "aram"
    }

    object Defaults {
        private val defaultConfig = AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig()
        val DEFAULT_ENABLED = defaultConfig.enabled
    }

    object ServiceConfig {
        const val ENABLED = "enabled"
    }

    object Payload {
        const val ORIGINAL_AD_REVENUE_KEY = "original_ad_revenue"
        const val ORIGINAL_AD_REVENUE_NO_VALUE = "no-value"
    }
}
