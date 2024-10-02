package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.utils.MainProcessDetector
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class AppMetricaDefaultAnonymousConfigProvider {

    private val tag = "[AppMetricaDefaultAnonymousConfigProvider]"

    private val anonymousApiKey: String = "629a824d-c717-4ba5-bc0f-3f3968554d01"
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
