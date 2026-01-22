package io.appmetrica.analytics.impl.location.stub

import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractor

internal class LastKnownExtractorStub : LastKnownLocationExtractor {

    override fun updateLastKnownLocation() {
        // Do nothing
    }
}
