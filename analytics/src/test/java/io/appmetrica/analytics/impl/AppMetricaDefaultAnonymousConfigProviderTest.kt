package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.impl.utils.FirstLaunchDetector
import io.appmetrica.analytics.impl.utils.process.CurrentProcessDetector
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.whenever

class AppMetricaDefaultAnonymousConfigProviderTest : CommonTest() {

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private lateinit var currentProcessDetector: CurrentProcessDetector
    private lateinit var firstLaunchDetector: FirstLaunchDetector

    private val apiKey = "629a824d-c717-4ba5-bc0f-3f3968554d01"

    private val appMetricaDefaultAnonymousConfigProvider: AppMetricaDefaultAnonymousConfigProvider by setUp {
        AppMetricaDefaultAnonymousConfigProvider()
    }

    @Before
    fun setUp() {
        firstLaunchDetector = ClientServiceLocator.getInstance().firstLaunchDetector
        currentProcessDetector = ClientServiceLocator.getInstance().currentProcessDetector
        whenever(currentProcessDetector.isMainProcess()).thenReturn(true)
    }

    @Test
    fun getConfigForDefaultConfig() {
        val config = appMetricaDefaultAnonymousConfigProvider.getConfig(
            AppMetricaLibraryAdapterConfig.newConfigBuilder()
                .withAdvIdentifiersTracking(false)
                .build()
        )
        assertThat(config)
            .usingRecursiveComparison()
            .isEqualTo(
                AppMetricaConfig.newConfigBuilder(apiKey)
                    .withAdvIdentifiersTracking(false)
                    .build()
            )
    }

    @Test
    fun getConfigForDefaultConfigForEnabledAdvIdTracking() {
        val config = appMetricaDefaultAnonymousConfigProvider.getConfig(
            AppMetricaLibraryAdapterConfig.newConfigBuilder()
                .withAdvIdentifiersTracking(true)
                .build()
        )
        assertThat(config)
            .usingRecursiveComparison()
            .isEqualTo(
                AppMetricaConfig.newConfigBuilder(apiKey)
                    .withAdvIdentifiersTracking(true)
                    .build()
            )
    }

    @Test
    fun `getConfig for main process and not first launch`() {
        whenever(firstLaunchDetector.isNotFirstLaunch()).thenReturn(true)
        val config = appMetricaDefaultAnonymousConfigProvider.getConfig(
            AppMetricaLibraryAdapterConfig.newConfigBuilder().build()
        )
        assertThat(config)
            .usingRecursiveComparison()
            .isEqualTo(
                AppMetricaConfig.newConfigBuilder(apiKey)
                    .handleFirstActivationAsUpdate(true)
                    .build()
            )
    }

    @Test
    fun `getConfig for non main process and not first launch`() {
        whenever(firstLaunchDetector.isNotFirstLaunch()).thenReturn(true)
        whenever(currentProcessDetector.isMainProcess()).thenReturn(false)
        val config = appMetricaDefaultAnonymousConfigProvider.getConfig(
            AppMetricaLibraryAdapterConfig.newConfigBuilder().build()
        )
        assertThat(config)
            .usingRecursiveComparison()
            .isEqualTo(
                AppMetricaConfig.newConfigBuilder(apiKey)
                    .build()
            )
    }
}
