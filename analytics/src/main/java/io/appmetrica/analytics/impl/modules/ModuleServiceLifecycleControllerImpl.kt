package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.AppMetricaServiceLifecycle
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceLifecycleController
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceLifecycleObserver

class ModuleServiceLifecycleControllerImpl(
    private val serviceLifecycle: AppMetricaServiceLifecycle
) : ModuleServiceLifecycleController {

    override fun registerObserver(observer: ModuleServiceLifecycleObserver) {
        serviceLifecycle.addFirstClientConnectObserver { observer.onFirstClientConnected() }
        serviceLifecycle.addAllClientDisconnectedObserver { observer.onAllClientsDisconnected() }
    }
}
