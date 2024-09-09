package io.appmetrica.analytics.modulesapi.internal.client

interface ServiceConfigUpdateListener<T : Any> {
    fun onServiceConfigUpdated(config: ModuleServiceConfig<T?>)
}
