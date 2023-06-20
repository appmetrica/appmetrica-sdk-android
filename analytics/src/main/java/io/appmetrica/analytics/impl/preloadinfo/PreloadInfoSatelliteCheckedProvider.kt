package io.appmetrica.analytics.impl.preloadinfo

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.SatelliteCheckedProvider
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage

internal class PreloadInfoSatelliteCheckedProvider @VisibleForTesting constructor(
    private val servicePreferences: PreferencesServiceDbStorage
) : SatelliteCheckedProvider {

    constructor() : this(GlobalServiceLocator.getInstance().servicePreferences)

    override fun wasSatelliteChecked(): Boolean = servicePreferences.wasSatellitePreloadInfoChecked()

    override fun markSatelliteChecked() {
        servicePreferences.markSatellitePreloadInfoChecked().commit()
    }
}
