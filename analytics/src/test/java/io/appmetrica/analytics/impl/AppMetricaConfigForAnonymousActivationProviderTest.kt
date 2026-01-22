package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

internal class AppMetricaConfigForAnonymousActivationProviderTest : CommonTest() {

    private val apiKey = UUID.randomUUID().toString()
    private val configFromPreferences = AppMetricaConfig.newConfigBuilder(apiKey)
        .build()
    private val configFromPreferencesWithAdvTracking = AppMetricaConfig.newConfigBuilder(apiKey)
        .withAdvIdentifiersTracking(true)
        .build()
    private val configBuilderFromPreferences: AppMetricaConfig.Builder = mock()

    private val configFromLibraryAdapter = AppMetricaConfig.newConfigBuilder(apiKey)
        .build()
    private val configFromLibraryAdapterWithAdvTracking = AppMetricaConfig.newConfigBuilder(apiKey)
        .withAdvIdentifiersTracking(true)
        .build()

    private val preferences: PreferencesClientDbStorage = mock {
        on { appMetricaConfig } doReturn configBuilderFromPreferences
    }
    private val adapterConfig: AppMetricaLibraryAdapterConfig = mock()

    @get:Rule
    val defaultAnonymousConfigProviderMockedConstructionRule =
        constructionRule<AppMetricaDefaultAnonymousConfigProvider>()
    private val defaultAnonymousConfigProvider by defaultAnonymousConfigProviderMockedConstructionRule

    private val configProvider: AppMetricaConfigForAnonymousActivationProvider by setUp {
        AppMetricaConfigForAnonymousActivationProvider(preferences)
    }

    @Test
    fun `config if exists in preferences with advIdentifiersTracking in preferences`() {
        whenever(defaultAnonymousConfigProvider.getConfig(adapterConfig))
            .thenReturn(configFromLibraryAdapter)
        whenever(configBuilderFromPreferences.build()).thenReturn(configFromPreferencesWithAdvTracking)

        assertThat(configProvider.getConfig(adapterConfig)).isEqualTo(configFromPreferencesWithAdvTracking)
        verify(configBuilderFromPreferences, never()).withAdvIdentifiersTracking(any())
    }

    @Test
    fun `config if exists in preferences without advIdentifiersTracking in preferences`() {
        whenever(defaultAnonymousConfigProvider.getConfig(adapterConfig))
            .thenReturn(configFromLibraryAdapterWithAdvTracking)
        whenever(configBuilderFromPreferences.build()).thenReturn(configFromPreferences)

        assertThat(configProvider.getConfig(adapterConfig)).isEqualTo(configFromPreferences)
        verify(configBuilderFromPreferences).withAdvIdentifiersTracking(true)
    }

    @Test
    fun `config if exists in preferences without advIdentifiersTracking in preferences and adapter`() {
        whenever(defaultAnonymousConfigProvider.getConfig(adapterConfig))
            .thenReturn(configFromLibraryAdapter)
        whenever(configBuilderFromPreferences.build()).thenReturn(configFromPreferences)

        verify(configBuilderFromPreferences, never()).withAdvIdentifiersTracking(any())
        assertThat(configProvider.getConfig(adapterConfig)).isEqualTo(configFromPreferences)
    }

    @Test
    fun `config if doesn't exist in preferences`() {
        whenever(defaultAnonymousConfigProvider.getConfig(adapterConfig))
            .thenReturn(configFromLibraryAdapter)
        whenever(preferences.appMetricaConfig).thenReturn(null)

        assertThat(configProvider.getConfig(adapterConfig)).isEqualTo(configFromLibraryAdapter)
    }
}
