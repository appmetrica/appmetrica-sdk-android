package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage

internal class AppMetricaConfigForAnonymousActivationProvider(
    private val preferences: PreferencesClientDbStorage
) {

    private val defaultAnonymousConfigProvider = AppMetricaDefaultAnonymousConfigProvider()

    val config: AppMetricaConfig
        get() = preferences.appMetricaConfig ?: defaultAnonymousConfigProvider.config
}
