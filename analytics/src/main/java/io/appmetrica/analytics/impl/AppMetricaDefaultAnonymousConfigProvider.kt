package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.utils.FirstLaunchDetector
import io.appmetrica.analytics.impl.utils.MainProcessDetector

class AppMetricaDefaultAnonymousConfigProvider {

    private val anonymousApiKey: String = "629a824d-c717-4ba5-bc0f-3f3968554d01"
    private val mainProcessDetector: MainProcessDetector = ClientServiceLocator.getInstance().mainProcessDetector
    private val firstLaunchDetector = FirstLaunchDetector()

    fun getConfig(context: Context): AppMetricaConfig {
        val builder = AppMetricaConfig.newConfigBuilder(anonymousApiKey)
        if (mainProcessDetector.isMainProcess && firstLaunchDetector.detectNotFirstLaunch(context)) {
            builder.handleFirstActivationAsUpdate(true)
        }
        return builder.build()
    }
}
