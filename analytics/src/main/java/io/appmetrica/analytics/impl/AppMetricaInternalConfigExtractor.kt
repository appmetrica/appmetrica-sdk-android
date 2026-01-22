package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig

internal object AppMetricaInternalConfigExtractor {

    private const val CLIDS_KEY = "YMM_clids"
    private const val DISTRIBUTION_REFERRER_KEY = "YMM_distributionReferrer"
    private const val PRELOAD_INFO_AUTO_TRACKING_KEY = "YMM_preloadInfoAutoTracking"

    @JvmStatic
    fun getClids(config: AppMetricaConfig): Map<String, String?>? {
        @Suppress("UNCHECKED_CAST")
        return config.additionalConfig[CLIDS_KEY] as? Map<String, String?>
    }

    @JvmStatic
    fun getDistributionReferrer(config: AppMetricaConfig): String? {
        return config.additionalConfig[DISTRIBUTION_REFERRER_KEY] as String?
    }

    @JvmStatic
    fun getPreloadInfoAutoTracking(config: AppMetricaConfig): Boolean? {
        return config.additionalConfig[PRELOAD_INFO_AUTO_TRACKING_KEY] as Boolean?
    }
}
