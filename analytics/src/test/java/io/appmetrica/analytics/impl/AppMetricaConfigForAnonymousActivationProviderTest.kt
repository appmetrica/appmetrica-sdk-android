package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
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

    private val context: Context = mock()
    private val configFromPreferences: AppMetricaConfig = mock()
    private val defaultConfig: AppMetricaConfig = mock()
    private val preferences: PreferencesClientDbStorage = mock()

    @get:Rule
    val defaultAnonymousConfigProviderMockedConstructionRule =
        constructionRule<AppMetricaDefaultAnonymousConfigProvider> {
            on { getConfig(context) } doReturn defaultConfig
        }

    private val configProvider: AppMetricaConfigForAnonymousActivationProvider by setUp {
        AppMetricaConfigForAnonymousActivationProvider(context, preferences)
    }

    @Test
    fun `config if exists in preferences`() {
        whenever(preferences.appMetricaConfig).thenReturn(configFromPreferences)
        assertThat(configProvider.config).isEqualTo(configFromPreferences)
    }

    @Test
    fun `config if doesn't exist in preferences`() {
        assertThat(configProvider.config).isEqualTo(defaultConfig)
    }
}
