package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class AppMetricaConfigForAnonymousActivationProvider(
    private val preferences: PreferencesClientDbStorage
) {

    private val tag = "[AppMetricaConfigForAnonymousActivationProvider]"

    private val defaultAnonymousConfigProvider = AppMetricaDefaultAnonymousConfigProvider()

    val config: AppMetricaConfig
        get() {
            val configFromPreferences = preferences.appMetricaConfig
            if (configFromPreferences != null) {
                DebugLogger.info(tag, "Choose saved config")
                return configFromPreferences
            }
            DebugLogger.info(tag, "Choose default anonymous config")
            return defaultAnonymousConfigProvider.getConfig()
        }
}
