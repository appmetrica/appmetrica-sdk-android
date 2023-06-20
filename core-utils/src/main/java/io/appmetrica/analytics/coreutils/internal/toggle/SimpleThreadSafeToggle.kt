package io.appmetrica.analytics.coreutils.internal.toggle

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.control.ToggleObserver
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

abstract class SimpleThreadSafeToggle(
    initialState: Boolean = false,
    protected val tag: String
) : Toggle {

    private var _actualState: Boolean = initialState

    override val actualState: Boolean
        @Synchronized
        get() = _actualState

    private val observers = ArrayList<ToggleObserver>()

    init {
        YLogger.info(tag, "Initial state = $initialState")
    }

    @Synchronized
    override fun registerObserver(toggleObserver: ToggleObserver, sticky: Boolean) {
        observers.add(toggleObserver)
        if (sticky) {
            toggleObserver.onStateChanged(actualState)
        }
    }

    @Synchronized
    override fun removeObserver(toggleObserver: ToggleObserver) {
        observers.remove(toggleObserver)
    }

    @Synchronized
    protected fun updateState(value: Boolean) {
        if (value != actualState) {
            YLogger.info(tag, "Notify update state from $actualState -> $value")
            _actualState = value
            observers.forEach { it.onStateChanged(value) }
        }
    }
}
