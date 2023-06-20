package io.appmetrica.analytics.impl.location.stub

import io.appmetrica.analytics.locationapi.internal.LocationReceiver

class LocationReceiverStub : LocationReceiver {

    override fun startLocationUpdates() {
        // Do nothing
    }

    override fun stopLocationUpdates() {
        // Do nothing
    }
}
