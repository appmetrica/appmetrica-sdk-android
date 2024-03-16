package io.appmetrica.analytics.modulesapi.internal.service

interface ModuleServiceLifecycleObserver {

    fun onFirstClientConnected()

    fun onAllClientsDisconnected()
}
