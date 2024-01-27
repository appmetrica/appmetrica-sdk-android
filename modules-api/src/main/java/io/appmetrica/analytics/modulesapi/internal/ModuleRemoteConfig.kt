package io.appmetrica.analytics.modulesapi.internal

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers

data class ModuleRemoteConfig<T>(
    val identifiers: SdkIdentifiers,
    val remoteConfigMetaInfo: RemoteConfigMetaInfo,
    val featuresConfig: T
)
