package io.appmetrica.analytics.location.impl

import io.appmetrica.analytics.location.impl.system.PassiveLocationReceiverProvider
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProviderFactory

internal class LocationReceiverProviderFactoryImpl(
    override val passiveLocationReceiverProvider: PassiveLocationReceiverProvider
) : LocationReceiverProviderFactory
