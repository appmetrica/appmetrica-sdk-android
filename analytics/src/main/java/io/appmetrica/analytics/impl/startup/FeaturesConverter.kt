package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.FeaturesResult

internal class FeaturesConverter {

    fun convert(internal: FeaturesInternal): FeaturesResult = FeaturesResult(
        internal.sslPinning
    )
}
