package io.appmetrica.analytics.impl.request

/**
 * Query-parameter encoding for startup `hoyas` (`UrlParts.YANDEX_ADS_ONLY`).
 * Persistence of last-sent uses [io.appmetrica.analytics.impl.startup.StartupOptionalBoolConverter], not this.
 */
internal object YandexAdsOnlyParameter {
    private const val TRUE = "1"
    private const val FALSE = "0"

    @JvmStatic
    fun toQueryValue(isYandexAdsOnly: Boolean): String =
        if (isYandexAdsOnly) TRUE else FALSE
}
