package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils

internal class YandexAdsSDKDetector(
    private val classExistsChecker: (String) -> Boolean = ReflectionUtils::detectClassExists
) {

    val isPresent: Boolean by lazy { classExistsChecker(MARKER_CLASS_NAME) }

    companion object {
        const val MARKER_CLASS_NAME = "com.monetization.ads.AnalyticsAdsMarker"

        @JvmStatic
        val shared: YandexAdsSDKDetector by lazy { YandexAdsSDKDetector() }
    }
}
