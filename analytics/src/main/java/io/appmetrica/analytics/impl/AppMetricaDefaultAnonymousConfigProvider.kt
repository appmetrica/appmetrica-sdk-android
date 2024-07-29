package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig

class AppMetricaDefaultAnonymousConfigProvider {

    private val anonymousApiKey: String = "629a824d-c717-4ba5-bc0f-3f3968554d01"

    val config: AppMetricaConfig
        get() = AppMetricaConfig.newConfigBuilder(anonymousApiKey).build()
}
