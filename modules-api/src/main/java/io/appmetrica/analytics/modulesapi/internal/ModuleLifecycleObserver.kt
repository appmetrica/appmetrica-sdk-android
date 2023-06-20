package io.appmetrica.analytics.modulesapi.internal

interface ModuleLifecycleObserver {

    fun onFirstClientConnected()

    fun onAllClientsDisconnected()
}
