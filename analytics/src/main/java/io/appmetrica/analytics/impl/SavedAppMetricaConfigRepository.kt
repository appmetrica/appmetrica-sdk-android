package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class SavedAppMetricaConfigRepository(
    private val preferences: PreferencesClientDbStorage,
    private val timeProvider: TimeProvider = SystemTimeProvider()
) {

    private val tag = "[SavedAppMetricaConfigRepository]"

    fun getValidSavedConfig(): AppMetricaConfig.Builder? {
        val config = preferences.appMetricaConfig
        if (config == null) {
            if (preferences.appMetricaConfigSavedAt != null) {
                DebugLogger.info(tag, "Orphan timestamp without config. Clearing.")
                preferences.clearAppMetricaConfig()
            }
            return null
        }

        val savedAt = preferences.appMetricaConfigSavedAt
        val now = timeProvider.currentTimeMillis()
        if (savedAt == null) {
            DebugLogger.info(tag, "No timestamp for saved config. Lazy migrate with now=$now")
            preferences.setAppMetricaConfigSavedAt(now)
            return config
        }

        if (SavedAppMetricaConfigTtlChecker.isExpired(savedAt, now)) {
            DebugLogger.info(tag, "Saved config expired. savedAt=$savedAt, now=$now. Clearing.")
            preferences.clearAppMetricaConfig()
            return null
        }

        return config
    }
}
