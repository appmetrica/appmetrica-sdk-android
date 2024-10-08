package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.utils.FirstLaunchDetector
import io.appmetrica.analytics.impl.utils.MainProcessDetector
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class AppMetricaDefaultAnonymousConfigProviderTest : CommonTest() {

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private lateinit var mainProcessDetector: MainProcessDetector
    private lateinit var firstLaunchDetector: FirstLaunchDetector

    private val apiKey = "629a824d-c717-4ba5-bc0f-3f3968554d01"

    private val appMetricaDefaultAnonymousConfigProvider: AppMetricaDefaultAnonymousConfigProvider by setUp {
        AppMetricaDefaultAnonymousConfigProvider()
    }

    @Before
    fun setUp() {
        firstLaunchDetector = ClientServiceLocator.getInstance().firstLaunchDetector
        mainProcessDetector = ClientServiceLocator.getInstance().mainProcessDetector
        whenever(mainProcessDetector.isMainProcess).thenReturn(true)
    }

    @Test
    fun getConfig() {
        assertThat(appMetricaDefaultAnonymousConfigProvider.getConfig())
            .usingRecursiveComparison()
            .isEqualTo(AppMetricaConfig.newConfigBuilder(apiKey).build())
    }

    @Test
    fun `getConfig for main process and not first launch`() {
        whenever(firstLaunchDetector.isNotFirstLaunch()).thenReturn(true)
        assertThat(appMetricaDefaultAnonymousConfigProvider.getConfig())
            .usingRecursiveComparison()
            .isEqualTo(AppMetricaConfig.newConfigBuilder(apiKey).handleFirstActivationAsUpdate(true).build())
    }

    @Test
    fun `getConfig for non main process and not first launch`() {
        whenever(firstLaunchDetector.isNotFirstLaunch()).thenReturn(true)
        whenever(mainProcessDetector.isMainProcess).thenReturn(false)
        assertThat(appMetricaDefaultAnonymousConfigProvider.getConfig())
            .usingRecursiveComparison()
            .isEqualTo(AppMetricaConfig.newConfigBuilder(apiKey).build())
    }
}
