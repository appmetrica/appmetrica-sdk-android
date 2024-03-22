package io.appmetrica.analytics.impl.modules.service

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.impl.modules.RemoteConfigMetaInfoModel
import io.appmetrica.analytics.impl.startup.StartupState

internal class ModuleRemoteConfigProvider(private val startupState: StartupState) {

    private val commonIdentifiers = SdkIdentifiers(
        startupState.uuid,
        startupState.deviceId,
        startupState.deviceIdHash
    )
    private val remoteConfigMetaInfo =
        RemoteConfigMetaInfoModel(startupState.firstStartupServerTime, startupState.obtainServerTime)

    fun getRemoteConfigForServiceModule(identifier: String) = ServiceModuleRemoteConfigModel(
        identifiers = commonIdentifiers,
        remoteConfigMetaInfo = remoteConfigMetaInfo,
        featuresConfig = startupState.modulesRemoteConfigs[identifier]
    )
}
