package io.appmetrica.analytics.modulesapi.internal

interface RemoteConfigUpdateListener<T : Any> {
    fun onRemoteConfigUpdated(config: ModuleRemoteConfig<T?>)
}
