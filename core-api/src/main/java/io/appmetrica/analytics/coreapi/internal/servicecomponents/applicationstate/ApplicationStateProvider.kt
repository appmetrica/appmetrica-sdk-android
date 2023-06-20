package io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate

interface ApplicationStateProvider {

    val currentState: ApplicationState

    fun registerStickyObserver(observer: ApplicationStateObserver): ApplicationState
}
