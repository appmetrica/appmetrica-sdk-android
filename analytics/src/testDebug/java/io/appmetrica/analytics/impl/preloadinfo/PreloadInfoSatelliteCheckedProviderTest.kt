package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.random.Random

internal class PreloadInfoSatelliteCheckedProviderTest : CommonTest() {

    private val checked = Random.nextBoolean()

    private val servicePreferences = mock<PreferencesServiceDbStorage> {
        on { wasSatellitePreloadInfoChecked() } doReturn checked
    }
    private val provider = PreloadInfoSatelliteCheckedProvider(servicePreferences)

    @Test
    fun wasSatelliteChecked() {
        assertThat(provider.wasSatelliteChecked()).isEqualTo(checked)
    }

    @Test
    fun markSatelliteChecked() {
        provider.markSatelliteChecked()
        verify(servicePreferences).markSatellitePreloadInfoChecked()
    }
}
