package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.time.Duration.Companion.days

internal class SavedAppMetricaConfigRepositoryTest : CommonTest() {

    private val preferences: PreferencesClientDbStorage = mock()
    private val timeProvider: TimeProvider = mock()
    private val repository by setUp { SavedAppMetricaConfigRepository(preferences, timeProvider) }

    private val now = 1_700_000_000_000L
    private val ttl = 30.days.inWholeMilliseconds
    private val configBuilder = AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString())

    @Test
    fun getValidSavedConfigReturnsNullWhenConfigIsMissing() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(now)
        whenever(preferences.appMetricaConfig).thenReturn(null)
        whenever(preferences.appMetricaConfigSavedAt).thenReturn(null)

        assertThat(repository.getValidSavedConfig()).isNull()
        verify(preferences, never()).clearAppMetricaConfig()
    }

    @Test
    fun getValidSavedConfigClearsWhenSavedAtExistsWithoutConfig() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(now)
        whenever(preferences.appMetricaConfig).thenReturn(null)
        whenever(preferences.appMetricaConfigSavedAt).thenReturn(now)

        assertThat(repository.getValidSavedConfig()).isNull()
        verify(preferences).clearAppMetricaConfig()
    }

    @Test
    fun getValidSavedConfigBackfillsSavedAtWhenMissing() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(now)
        whenever(preferences.appMetricaConfig).thenReturn(configBuilder)
        whenever(preferences.appMetricaConfigSavedAt).thenReturn(null)

        assertThat(repository.getValidSavedConfig()).isSameAs(configBuilder)
        verify(preferences).setAppMetricaConfigSavedAt(now)
        verify(preferences, never()).clearAppMetricaConfig()
    }

    @Test
    fun getValidSavedConfigReturnsConfigWhenWithinTtl() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(now)
        whenever(preferences.appMetricaConfig).thenReturn(configBuilder)
        whenever(preferences.appMetricaConfigSavedAt).thenReturn(now - 10.days.inWholeMilliseconds)

        assertThat(repository.getValidSavedConfig()).isSameAs(configBuilder)
        verify(preferences, never()).clearAppMetricaConfig()
        verify(preferences, never()).setAppMetricaConfigSavedAt(now)
    }

    @Test
    fun getValidSavedConfigReturnsConfigWhenSavedAtIsInFuture() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(now)
        whenever(preferences.appMetricaConfig).thenReturn(configBuilder)
        whenever(preferences.appMetricaConfigSavedAt).thenReturn(now + 1)

        assertThat(repository.getValidSavedConfig()).isSameAs(configBuilder)
        verify(preferences, never()).clearAppMetricaConfig()
    }

    @Test
    fun getValidSavedConfigClearsWhenExpired() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(now)
        whenever(preferences.appMetricaConfig).thenReturn(configBuilder)
        whenever(preferences.appMetricaConfigSavedAt).thenReturn(now - ttl)

        assertThat(repository.getValidSavedConfig()).isNull()
        verify(preferences).clearAppMetricaConfig()
    }
}
