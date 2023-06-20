package io.appmetrica.analytics.modulesapi.internal

import io.appmetrica.analytics.coreapi.internal.identifiers.Identifiers

data class ModuleRemoteConfig<T>(
    val identifiers: Identifiers,
    val remoteConfigMetaInfo: RemoteConfigMetaInfo,
    val featuresConfig: T
)
