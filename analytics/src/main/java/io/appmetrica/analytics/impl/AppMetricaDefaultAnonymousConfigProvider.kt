package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.utils.MainProcessDetector
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class AppMetricaDefaultAnonymousConfigProvider {

    private val tag = "[AppMetricaDefaultAnonymousConfigProvider]"

    private val anonymousApiKey: String = DefaultValues.ANONYMOUS_API_KEY
    private val mainProcessDetector: MainProcessDetector = ClientServiceLocator.getInstance().mainProcessDetector
    private val firstLaunchDetector = ClientServiceLocator.getInstance().firstLaunchDetector

    fun getConfig(): AppMetricaConfig {
        val builder = AppMetricaConfig.newConfigBuilder(anonymousApiKey)
        if (mainProcessDetector.isMainProcess && firstLaunchDetector.isNotFirstLaunch()) {
            DebugLogger.info(tag, "Add handleFirstActivationAsUpdate value to config")
            builder.handleFirstActivationAsUpdate(true)
        } else {
            DebugLogger.info(tag, "Use default config")
        }
        return builder.build()
    }
}
