package io.appmetrica.analytics.impl.modules.service

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigMetaInfo

internal data class ServiceModuleRemoteConfigModel<T>(
    override val identifiers: SdkIdentifiers,
    override val remoteConfigMetaInfo: RemoteConfigMetaInfo,
    override val featuresConfig: T
) : ModuleRemoteConfig<T>
