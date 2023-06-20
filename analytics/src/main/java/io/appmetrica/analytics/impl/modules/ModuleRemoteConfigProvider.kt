package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreapi.internal.identifiers.Identifiers
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.modulesapi.internal.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.RemoteConfigMetaInfo

internal class ModuleRemoteConfigProvider(private val startupState: StartupState) {

    private val commonIdentifiers = Identifiers(
        startupState.uuid,
        startupState.deviceId,
        startupState.deviceIdHash
    )
    private val remoteConfigMetaInfo =
        RemoteConfigMetaInfo(startupState.firstStartupServerTime, startupState.obtainServerTime)

    fun getRemoteConfigForModule(identifier: String) = ModuleRemoteConfig(
        identifiers = commonIdentifiers,
        remoteConfigMetaInfo = remoteConfigMetaInfo,
        featuresConfig = startupState.modulesRemoteConfigs[identifier]
    )
}
