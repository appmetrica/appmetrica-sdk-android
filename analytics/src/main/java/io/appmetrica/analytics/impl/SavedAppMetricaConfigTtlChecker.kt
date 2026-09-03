package io.appmetrica.analytics.impl

import kotlin.time.Duration.Companion.days

internal object SavedAppMetricaConfigTtlChecker {

    private val SAVED_APP_METRICA_CONFIG_TTL_MS: Long = 30.days.inWholeMilliseconds

    fun isExpired(savedAtMillis: Long, nowMillis: Long): Boolean {
        if (nowMillis < savedAtMillis) {
            return false
        }
        return nowMillis - savedAtMillis >= SAVED_APP_METRICA_CONFIG_TTL_MS
    }
}
