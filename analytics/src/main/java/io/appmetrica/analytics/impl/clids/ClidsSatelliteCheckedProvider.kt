package io.appmetrica.analytics.impl.clids

import io.appmetrica.analytics.impl.SatelliteCheckedProvider
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage

internal class ClidsSatelliteCheckedProvider(private val servicePreferences: PreferencesServiceDbStorage) :
    SatelliteCheckedProvider {

    override fun wasSatelliteChecked(): Boolean = servicePreferences.wereSatelliteClidsChecked()

    override fun markSatelliteChecked() {
        servicePreferences.markSatelliteClidsChecked().commit()
    }
}
