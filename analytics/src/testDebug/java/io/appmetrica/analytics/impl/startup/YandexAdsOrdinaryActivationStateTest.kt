package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class YandexAdsOrdinaryActivationStateTest : CommonTest() {

    private val storage = mock<PreferencesServiceDbStorage>()

    @Test
    fun onMainClientApiKeyMarksOrdinaryAndClearsOnAnonymous() {
        whenever(storage.hasOrdinaryActivation()).thenReturn(false)
        val state = YandexAdsOrdinaryActivationState(storage)
        assertThat(state.hasOrdinaryActivation()).isFalse

        state.onMainClientApiKey(null)
        assertThat(state.hasOrdinaryActivation()).isFalse
        verify(storage, never()).saveOrdinaryActivation(any())

        state.onMainClientApiKey(DefaultValues.ANONYMOUS_API_KEY)
        assertThat(state.hasOrdinaryActivation()).isFalse
        verify(storage, never()).saveOrdinaryActivation(any())

        state.onMainClientApiKey("ordinary-api-key")
        assertThat(state.hasOrdinaryActivation()).isTrue
        verify(storage).saveOrdinaryActivation(true)

        state.onMainClientApiKey(DefaultValues.ANONYMOUS_API_KEY)
        assertThat(state.hasOrdinaryActivation()).isFalse
        verify(storage).saveOrdinaryActivation(false)

        state.onMainClientApiKey("ordinary-api-key")
        assertThat(state.hasOrdinaryActivation()).isTrue
        verify(storage, times(2)).saveOrdinaryActivation(true)
    }

    @Test
    fun restoresOrdinaryFlagFromStorageAndClearsOnAnonymous() {
        whenever(storage.hasOrdinaryActivation()).thenReturn(true)
        val state = YandexAdsOrdinaryActivationState(storage)

        assertThat(state.hasOrdinaryActivation()).isTrue

        state.onMainClientApiKey(DefaultValues.ANONYMOUS_API_KEY)
        assertThat(state.hasOrdinaryActivation()).isFalse
        verify(storage).saveOrdinaryActivation(false)
    }
}
