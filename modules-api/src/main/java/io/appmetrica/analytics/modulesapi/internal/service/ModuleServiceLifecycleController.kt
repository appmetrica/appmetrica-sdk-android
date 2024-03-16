package io.appmetrica.analytics.modulesapi.internal.service

interface ModuleServiceLifecycleController {

    fun registerObserver(observer: ModuleServiceLifecycleObserver)
}
