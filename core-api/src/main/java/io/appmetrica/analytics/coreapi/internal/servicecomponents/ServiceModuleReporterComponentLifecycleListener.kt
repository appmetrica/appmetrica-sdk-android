package io.appmetrica.analytics.coreapi.internal.servicecomponents

interface ServiceModuleReporterComponentLifecycleListener {

    fun onMainReporterCreated(context: ServiceModuleReporterComponentContext)
}
