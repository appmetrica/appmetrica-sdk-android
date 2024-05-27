package io.appmetrica.analytics.coreutils.internal.toggle

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.control.ToggleObserver
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class ConjunctiveCompositeThreadSafeToggle(
    toggles: List<Toggle>,
    tagPostfix: String
) : Toggle {

    private val observers = ArrayList<ToggleObserver>()
    private val toggleStates = HashMap<ToggleObserver, Boolean>()

    private val tag = "[ConjunctiveCompositeToggle-$tagPostfix]"

    private val lock = ReentrantLock()

    @Volatile
    override var actualState: Boolean = false

    init {
        DebugLogger.info(tag, "Init...")
        withLock {
            toggles.forEach { toggle ->
                val observer = object : ToggleObserver {

                    override fun onStateChanged(incomingState: Boolean) {
                        withLock { updateState(this, incomingState, "${toggle::class.simpleName}") }
                    }
                }

                toggleStates[observer] = toggle.actualState
                toggle.registerObserver(observer, false)
            }
            actualState = calculateState(toggleStates.values)
            DebugLogger.info(tag, "Initial state = $actualState")
        }
        DebugLogger.info(tag, "Init finished...")
    }

    override fun registerObserver(toggleObserver: ToggleObserver, sticky: Boolean) {
        DebugLogger.info(tag, "Register observer {$toggleObserver} with sticky = $sticky")
        withLock {
            observers.add(toggleObserver)
            DebugLogger.info(tag, "Current observers count = ${observers.size}")
            if (sticky) {
                toggleObserver.onStateChanged(actualState)
            }
        }
    }

    override fun removeObserver(toggleObserver: ToggleObserver) {
        DebugLogger.info(tag, "Remove observer {$toggleObserver}")
        withLock {
            observers.remove(toggleObserver)
            DebugLogger.info(tag, "Current observers count = ${observers.size}")
        }
    }

    private fun updateState(observer: ToggleObserver, state: Boolean, desc: String) {
        DebugLogger.info(tag, "Update state for observer = $desc: ${toggleStates[observer]} -> $state")
        toggleStates[observer] = state
        notifyStateChanged()
    }

    private fun notifyStateChanged() {
        val incomingState = calculateState(toggleStates.values)
        DebugLogger.info(tag, "Notify state changed: $actualState -> $incomingState")
        if (incomingState != actualState) {
            actualState = incomingState
            notifyObservers(incomingState)
        }
    }

    private fun notifyObservers(incomingState: Boolean) {
        DebugLogger.info(
            tag,
            "Notify observers with status = $incomingState. Current count = ${observers.size}"
        )
        observers.forEach {
            it.onStateChanged(incomingState)
        }
    }

    private fun calculateState(actualStates: Collection<Boolean>): Boolean = actualStates.all { it }

    private inline fun withLock(block: () -> Unit) {
        try {
            acquireLock()
            block()
        } finally {
            releaseLock()
        }
    }

    private fun acquireLock() {
        var acquired = false
        while (!acquired) {
            runCatching {
                acquired = lock.tryLock(100, TimeUnit.MILLISECONDS)
            }
            if (!acquired) {
                DebugLogger.info(tag, "One more attempt to acquire lock after waiting")
                kotlin.runCatching { Thread.sleep(100) }
            }
        }
    }

    private fun releaseLock() {
        lock.unlock()
    }

    override fun toString(): String {
        return "ConjunctiveCompositeThreadSafeToggle(toggleStates=$toggleStates" +
            ", tag='$tag'" +
            ", actualState=$actualState)"
    }
}
