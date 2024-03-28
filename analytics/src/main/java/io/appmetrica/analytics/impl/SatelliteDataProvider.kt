package io.appmetrica.analytics.impl

internal interface SatelliteDataProvider<T> : Function0<T?> {
    val authority get() = "com.yandex.preinstallsatellite.appmetrica.provider"
}
