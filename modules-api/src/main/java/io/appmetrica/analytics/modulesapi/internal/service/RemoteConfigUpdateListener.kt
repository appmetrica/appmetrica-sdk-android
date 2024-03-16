package io.appmetrica.analytics.modulesapi.internal.service

interface RemoteConfigUpdateListener<T : Any> {
    fun onRemoteConfigUpdated(config: ModuleRemoteConfig<T?>)
}
