package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AppMetricaConfigForAnonymousActivationProviderTest : CommonTest() {

    private val configFromPreferences: AppMetricaConfig = mock()
    private val defaultConfig: AppMetricaConfig = mock()
    private val preferences: PreferencesClientDbStorage = mock()
    private val adapterConfig: AppMetricaLibraryAdapterConfig = mock()

    @get:Rule
    val defaultAnonymousConfigProviderMockedConstructionRule =
        constructionRule<AppMetricaDefaultAnonymousConfigProvider> {
            on { getConfig(adapterConfig) } doReturn defaultConfig
        }

    private val configProvider: AppMetricaConfigForAnonymousActivationProvider by setUp {
        AppMetricaConfigForAnonymousActivationProvider(preferences)
    }

    @Test
    fun `config if exists in preferences`() {
        whenever(preferences.appMetricaConfig).thenReturn(configFromPreferences)
        assertThat(configProvider.getConfig(adapterConfig)).isEqualTo(configFromPreferences)
    }

    @Test
    fun `config if doesn't exist in preferences`() {
        assertThat(configProvider.getConfig(adapterConfig)).isEqualTo(defaultConfig)
    }
}
