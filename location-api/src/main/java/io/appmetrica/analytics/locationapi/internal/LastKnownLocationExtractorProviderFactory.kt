package io.appmetrica.analytics.locationapi.internal

interface LastKnownLocationExtractorProviderFactory {

    val passiveLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider

    val gplLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider

    val networkLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider

    val gpsLastKnownLocationExtractorProvider: LastKnownLocationExtractorProvider
}
