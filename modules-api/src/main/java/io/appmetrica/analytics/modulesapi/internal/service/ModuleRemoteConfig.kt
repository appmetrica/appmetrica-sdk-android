package io.appmetrica.analytics.modulesapi.internal.service

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers

interface ModuleRemoteConfig<T> {
    val identifiers: SdkIdentifiers
    val remoteConfigMetaInfo: RemoteConfigMetaInfo
    val featuresConfig: T
}
