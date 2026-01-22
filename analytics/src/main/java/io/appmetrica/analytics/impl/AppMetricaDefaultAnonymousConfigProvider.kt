package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.impl.utils.process.CurrentProcessDetector
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class AppMetricaDefaultAnonymousConfigProvider {

    private val tag = "[AppMetricaDefaultAnonymousConfigProvider]"

    private val anonymousApiKey: String = DefaultValues.ANONYMOUS_API_KEY

    private val currentProcessDetector: CurrentProcessDetector =
        ClientServiceLocator.getInstance().currentProcessDetector

    private val firstLaunchDetector = ClientServiceLocator.getInstance().firstLaunchDetector

    fun getConfig(libraryAdapterConfig: AppMetricaLibraryAdapterConfig): AppMetricaConfig {
        val builder = AppMetricaConfig.newConfigBuilder(anonymousApiKey)
        if (currentProcessDetector.isMainProcess() && firstLaunchDetector.isNotFirstLaunch()) {
            DebugLogger.info(tag, "Add handleFirstActivationAsUpdate value to config")
            builder.handleFirstActivationAsUpdate(true)
        } else {
            DebugLogger.info(tag, "Use default config")
        }
        DebugLogger.info(tag, "Apply library adapter config: $libraryAdapterConfig")
        builder.withAdvIdentifiersTracking(
            libraryAdapterConfig.advIdentifiersTracking
                ?: DefaultValues.ANONYMOUS_DEFAULT_REPORT_ADV_IDENTIFIERS_ENABLED
        )
        return builder.build().also {
            DebugLogger.info(tag, "Result config: $it")
        }
    }
}
