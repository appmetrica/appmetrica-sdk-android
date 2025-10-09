package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentContext
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentLifecycle
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentLifecycleListener
import java.util.concurrent.CopyOnWriteArrayList

class ServiceModuleReporterComponentLifecycleImpl :
    ServiceModuleReporterComponentLifecycle, ServiceModuleReporterComponentLifecycleListener {

    private val listeners = CopyOnWriteArrayList<ServiceModuleReporterComponentLifecycleListener>()

    override fun subscribe(listener: ServiceModuleReporterComponentLifecycleListener) {
        listeners.add(listener)
    }

    override fun onMainReporterCreated(context: ServiceModuleReporterComponentContext) {
        listeners.forEach { it.onMainReporterCreated(context) }
    }
}
