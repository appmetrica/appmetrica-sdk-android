package io.appmetrica.analytics.impl.location

import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.locationapi.internal.LocationClient

internal class LocationClientProvider {

    fun getLocationClient(): LocationClient? = ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor(
        "io.appmetrica.analytics.location.internal.LocationClientImpl",
        LocationClient::class.java
    )
}
