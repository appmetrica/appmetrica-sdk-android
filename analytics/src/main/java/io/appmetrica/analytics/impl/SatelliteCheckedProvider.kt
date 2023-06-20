package io.appmetrica.analytics.impl

internal interface SatelliteCheckedProvider {

    fun wasSatelliteChecked(): Boolean

    fun markSatelliteChecked()
}
