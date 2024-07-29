package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AppMetricaDefaultAnonymousConfigProviderTest : CommonTest() {

    private val appMetricaDefaultAnonymousConfigProvider: AppMetricaDefaultAnonymousConfigProvider by setUp {
        AppMetricaDefaultAnonymousConfigProvider()
    }

    @Test
    fun config() {
        assertThat(appMetricaDefaultAnonymousConfigProvider.config)
            .usingRecursiveComparison()
            .isEqualTo(AppMetricaConfig.newConfigBuilder("629a824d-c717-4ba5-bc0f-3f3968554d01").build())
    }
}
