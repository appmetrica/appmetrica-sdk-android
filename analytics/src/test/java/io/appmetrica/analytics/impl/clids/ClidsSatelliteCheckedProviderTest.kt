package io.appmetrica.analytics.impl.clids

import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import kotlin.random.Random

internal class ClidsSatelliteCheckedProviderTest : CommonTest() {

    private val result = Random.nextBoolean()

    private val servicePreferences = mock<PreferencesServiceDbStorage> {
        on { wereSatelliteClidsChecked() } doReturn result
        on { markSatelliteClidsChecked() } doReturn mock
    }
    private val provider = ClidsSatelliteCheckedProvider(servicePreferences)

    @Test
    fun wasSatelliteChecked() {
        assertThat(provider.wasSatelliteChecked()).isEqualTo(result)
    }

    @Test
    fun markSatelliteChecked() {
        provider.markSatelliteChecked()
        inOrder(servicePreferences) {
            verify(servicePreferences).markSatelliteClidsChecked()
            verify(servicePreferences).commit()
        }
    }
}
