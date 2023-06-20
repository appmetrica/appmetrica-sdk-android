package io.appmetrica.analytics.impl.location.stub

import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProvider
import io.appmetrica.analytics.locationapi.internal.LastKnownLocationExtractorProviderFactory

class LastKnownExtractorProviderFactoryStub : LastKnownLocationExtractorProviderFactory {

    private val lastKnownExtractorProviderStub = LastKnownExtractorProviderStub()

    override val passiveLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider
        get() = lastKnownExtractorProviderStub
    override val gplLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider
        get() = lastKnownExtractorProviderStub
    override val networkLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider
        get() = lastKnownExtractorProviderStub
    override val gpsLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider
        get() = lastKnownExtractorProviderStub
}
