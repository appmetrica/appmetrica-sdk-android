package io.appmetrica.analytics.locationapi.internal

import android.location.Location

interface LocationProvider {

    val systemLocation: Location?

    val userLocation: Location?
}
