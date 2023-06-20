package io.appmetrica.analytics.testutils

import android.location.Location

object LocationUtils {

    private const val PROVIDER_NONE = "NONE"

    fun createFakeLocation(latitude: Double, longitude: Double): Location {
        val location = Location(PROVIDER_NONE)
        location.latitude = latitude
        location.longitude = longitude
        location.time = System.currentTimeMillis()
        return location
    }
}
