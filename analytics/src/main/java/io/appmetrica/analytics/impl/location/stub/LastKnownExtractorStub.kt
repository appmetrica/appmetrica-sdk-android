package io.appmetrica.analytics.impl.location.stub

import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractor

class LastKnownExtractorStub : LastKnownLocationExtractor {

    override fun updateLastKnownLocation() {
        // Do nothing
    }
}
