package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.AppMetricaServiceLifecycle
import io.appmetrica.analytics.modulesapi.internal.ModuleLifecycleController
import io.appmetrica.analytics.modulesapi.internal.ModuleLifecycleObserver

class ModuleLifecycleControllerImpl(
    private val serviceLifecycle: AppMetricaServiceLifecycle
) : ModuleLifecycleController {

    override fun registerObserver(observer: ModuleLifecycleObserver) {
        serviceLifecycle.addFirstClientConnectObserver { observer.onFirstClientConnected() }
        serviceLifecycle.addAllClientDisconnectedObserver { observer.onAllClientsDisconnected() }
    }
}
