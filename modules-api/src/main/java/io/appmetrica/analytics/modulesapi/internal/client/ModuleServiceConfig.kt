package io.appmetrica.analytics.modulesapi.internal.client

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers

interface ModuleServiceConfig<T> {
    val identifiers: SdkIdentifiers
    val featuresConfig: T
}
