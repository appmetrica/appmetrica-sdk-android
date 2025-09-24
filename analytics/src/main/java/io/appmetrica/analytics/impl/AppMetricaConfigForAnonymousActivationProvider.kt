package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class AppMetricaConfigForAnonymousActivationProvider(
    private val preferences: PreferencesClientDbStorage
) {

    private val tag = "[AppMetricaConfigForAnonymousActivationProvider]"

    private val defaultAnonymousConfigProvider = AppMetricaDefaultAnonymousConfigProvider()

    fun getConfig(libraryAdapterConfig: AppMetricaLibraryAdapterConfig): AppMetricaConfig {
        val configFromLibraryAdapter = defaultAnonymousConfigProvider.getConfig(libraryAdapterConfig)
        val configBuilderFromPreferences = preferences.appMetricaConfig
        if (configBuilderFromPreferences == null) {
            DebugLogger.info(
                tag,
                "Saved config is null. Choose config from library adapter: ${configFromLibraryAdapter.toJson()}"
            )
            return configFromLibraryAdapter
        }
        DebugLogger.info(tag, "Choose saved config")
        if (configBuilderFromPreferences.build().advIdentifiersTracking == null) {
            DebugLogger.info(
                tag,
                "Field advIdentifiersTracking is not set. " +
                    "Using value `${configFromLibraryAdapter.advIdentifiersTracking}` from library adapter"
            )
            configFromLibraryAdapter.advIdentifiersTracking?.let {
                configBuilderFromPreferences.withAdvIdentifiersTracking(it)
            }
        }
        return configBuilderFromPreferences.build().also {
            DebugLogger.info(tag, "Return config ${it.toJson()}")
        }
    }
}
