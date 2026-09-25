package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class YandexAdsStartupStateProviderTest : CommonTest() {

    @Test
    fun isYandexAdsOnlyTruthTable() {
        val missingMarker = YandexAdsStartupStateProvider(
            YandexAdsSDKDetector { false },
            ordinaryState(hasOrdinary = false)
        )
        assertThat(missingMarker.isYandexAdsOnly).isFalse

        val adsOnly = YandexAdsStartupStateProvider(
            YandexAdsSDKDetector { true },
            ordinaryState(hasOrdinary = false)
        )
        assertThat(adsOnly.isYandexAdsOnly).isTrue

        val ordinary = YandexAdsStartupStateProvider(
            YandexAdsSDKDetector { true },
            ordinaryState(hasOrdinary = true)
        )
        assertThat(ordinary.isYandexAdsOnly).isFalse
    }

    @Test
    fun requiresUpdateTransitionTable() {
        val provider = YandexAdsStartupStateProvider(
            YandexAdsSDKDetector { false },
            ordinaryState(hasOrdinary = false)
        )
        SoftAssertions().apply {
            assertThat(provider.requiresUpdate(false, null)).isFalse
            assertThat(provider.requiresUpdate(true, null)).isTrue
            assertThat(provider.requiresUpdate(false, false)).isFalse
            assertThat(provider.requiresUpdate(true, true)).isFalse
            assertThat(provider.requiresUpdate(true, false)).isTrue
            assertThat(provider.requiresUpdate(false, true)).isTrue
            assertAll()
        }
    }

    private fun ordinaryState(hasOrdinary: Boolean): YandexAdsOrdinaryActivationState {
        val storage = mock<PreferencesServiceDbStorage> {
            on { hasOrdinaryActivation() } doReturn hasOrdinary
        }
        return YandexAdsOrdinaryActivationState(storage)
    }
}
