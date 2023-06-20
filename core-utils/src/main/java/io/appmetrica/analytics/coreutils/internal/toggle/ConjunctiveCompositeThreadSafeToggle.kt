package io.appmetrica.analytics.coreutils.internal.toggle

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.control.ToggleObserver
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
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
        YLogger.info(tag, "Init...")
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
            YLogger.info(tag, "Initial state = $actualState")
        }
        YLogger.info(tag, "Init finished...")
    }

    override fun registerObserver(toggleObserver: ToggleObserver, sticky: Boolean) {
        YLogger.info(tag, "Register observer {$toggleObserver} with sticky = $sticky")
        withLock {
            observers.add(toggleObserver)
            YLogger.info(tag, "Current observers count = ${observers.size}")
            if (sticky) {
                toggleObserver.onStateChanged(actualState)
            }
        }
    }

    override fun removeObserver(toggleObserver: ToggleObserver) {
        YLogger.info(tag, "Remove observer {$toggleObserver}")
        withLock {
            observers.remove(toggleObserver)
            YLogger.info(tag, "Current observers count = ${observers.size}")
        }
    }

    private fun updateState(observer: ToggleObserver, state: Boolean, desc: String) {
        YLogger.info(tag, "Update state for observer = $desc: ${toggleStates[observer]} -> $state")
        toggleStates[observer] = state
        notifyStateChanged()
    }

    private fun notifyStateChanged() {
        val incomingState = calculateState(toggleStates.values)
        YLogger.info(tag, "Notify state changed: $actualState -> $incomingState")
        if (incomingState != actualState) {
            actualState = incomingState
            notifyObservers(incomingState)
        }
    }

    private fun notifyObservers(incomingState: Boolean) {
        YLogger.info(tag, "Notify observers with status = $incomingState. Current count = ${observers.size}")
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
                YLogger.info(tag, "One more attempt to acquire lock after waiting")
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
