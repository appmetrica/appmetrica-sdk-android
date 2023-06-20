package io.appmetrica.analytics.modulesapi.internal

interface ModuleLifecycleController {

    fun registerObserver(observer: ModuleLifecycleObserver)
}
