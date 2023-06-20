package io.appmetrica.analytics.coreapi.internal.control

interface Toggle {

    val actualState: Boolean

    fun registerObserver(toggleObserver: ToggleObserver, sticky: Boolean)

    fun removeObserver(toggleObserver: ToggleObserver)
}
