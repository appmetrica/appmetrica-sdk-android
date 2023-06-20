package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import kotlin.random.Random

class PreloadInfoSatelliteCheckedProviderTest : CommonTest() {

    private val checked = Random.nextBoolean()

    private val servicePreferences = mock<PreferencesServiceDbStorage> {
        on { wasSatellitePreloadInfoChecked() } doReturn checked
        on { markSatellitePreloadInfoChecked() } doReturn mock
    }
    private val provider = PreloadInfoSatelliteCheckedProvider(servicePreferences)

    @Test
    fun wasSatelliteChecked() {
        assertThat(provider.wasSatelliteChecked()).isEqualTo(checked)
    }

    @Test
    fun markSatelliteChecked() {
        provider.markSatelliteChecked()
        inOrder(servicePreferences) {
            verify(servicePreferences).markSatellitePreloadInfoChecked()
            verify(servicePreferences).commit()
        }
    }
}
