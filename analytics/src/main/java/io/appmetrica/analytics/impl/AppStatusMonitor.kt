package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class AppStatusMonitor {

    internal interface Observer {
        fun onResume()
        fun onPause()
    }

    private val tag = "[AppStatusMonitor]"

    private val observers = mutableSetOf<ObserverWrapper>()
    private var paused = true

    @Synchronized
    fun resume() {
        DebugLogger.info(tag, "resume")
        paused = false
        for (observer in observers) {
            observer.notifyOnResume()
        }
    }

    @Synchronized
    fun pause() {
        DebugLogger.info(tag, "pause")
        paused = true
        for (observer in observers) {
            observer.notifyOnPause()
        }
    }

    @Synchronized
    fun registerObserver(observer: Observer, timeout: Long) {
        registerObserver(observer, timeout, false)
    }

    @Synchronized
    fun registerObserver(observer: Observer, timeout: Long, sticky: Boolean) {
        val observerWrapper = ObserverWrapper(
            observer,
            ClientServiceLocator.getInstance().clientExecutorProvider.defaultExecutor,
            timeout
        )
        observers.add(observerWrapper)
        // ObserverWrapper is paused by default and do not need to call notifyOnPause
        if (sticky && !paused) {
            observerWrapper.notifyOnResume()
        }
    }

    @Synchronized
    fun unregisterObserver(observer: Observer) {
        observers.removeAll { it.observer == observer }
    }

    private class ObserverWrapper(val observer: Observer, val executor: ICommonExecutor, val timeout: Long) {
        private val tag = "[AppStatusMonitor.ObserverWrapper]"

        private var paused = true

        private val pauseRunnable = Runnable {
            DebugLogger.info(tag, "Notify on pause")
            observer.onPause()
        }

        fun notifyOnResume() {
            if (paused) {
                paused = false
                executor.remove(pauseRunnable)
                DebugLogger.info(tag, "Notify on resume")
                observer.onResume()
            }
        }

        fun notifyOnPause() {
            if (!paused) {
                paused = true
                DebugLogger.info(tag, "Schedule pause runnable with %s seconds delay", timeout)
                executor.executeDelayed(pauseRunnable, timeout)
            }
        }
    }
}
