package io.appmetrica.analytics.impl

internal interface SatelliteDataProvider<T> : Function0<T?> {
    fun authority() = "com.yandex.preinstallsatellite.appmetrica.provider"
}
