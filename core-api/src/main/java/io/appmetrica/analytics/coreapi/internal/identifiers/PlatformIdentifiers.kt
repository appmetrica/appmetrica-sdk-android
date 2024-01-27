package io.appmetrica.analytics.coreapi.internal.identifiers

data class PlatformIdentifiers(
    val advIdentifiersProvider: SimpleAdvertisingIdGetter,
    val appSetIdProvider: AppSetIdProvider
)
