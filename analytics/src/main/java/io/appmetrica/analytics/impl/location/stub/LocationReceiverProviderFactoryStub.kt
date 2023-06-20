package io.appmetrica.analytics.impl.location.stub

import io.appmetrica.analytics.locationapi.internal.LocationReceiverProvider
import io.appmetrica.analytics.locationapi.internal.LocationReceiverProviderFactory

class LocationReceiverProviderFactoryStub : LocationReceiverProviderFactory {

    override val passiveLocationReceiverProvider: LocationReceiverProvider =
        LocationReceiverProviderStub()
}
