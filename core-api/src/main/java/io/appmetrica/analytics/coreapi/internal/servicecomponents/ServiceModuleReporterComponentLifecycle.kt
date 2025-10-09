package io.appmetrica.analytics.coreapi.internal.servicecomponents

interface ServiceModuleReporterComponentLifecycle {

    fun subscribe(listener: ServiceModuleReporterComponentLifecycleListener)
}
