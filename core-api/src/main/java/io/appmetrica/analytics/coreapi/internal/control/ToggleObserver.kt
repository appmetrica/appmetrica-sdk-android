package io.appmetrica.analytics.coreapi.internal.control

fun interface ToggleObserver {

    fun onStateChanged(incomingState: Boolean)
}
