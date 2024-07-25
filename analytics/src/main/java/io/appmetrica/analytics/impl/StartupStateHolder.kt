package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.CopyOnWriteArrayList

internal class StartupStateHolder : StartupStateObserver {

    private val tag = "[StartupStateHolder]"

    @Volatile
    private lateinit var startupState: StartupState

    private val observers = CopyOnWriteArrayList<StartupStateObserver>()

    fun init(context: Context) {
        DebugLogger.info(tag, "Init")
        onStartupStateChanged(StartupState.Storage(context).read())
    }

    override fun onStartupStateChanged(startupState: StartupState) {
        DebugLogger.info(tag, "onStartupStateChanged. Observers count = ${observers.size}")
        this.startupState = startupState
        observers.forEach { it.onStartupStateChanged(startupState) }
    }

    fun getStartupState(): StartupState = startupState

    fun registerObserver(observer: StartupStateObserver) {
        observers.add(observer)
        DebugLogger.info(tag, "Register observer $observer. Total count = ${observers.size}")
        if (this::startupState.isInitialized) {
            observer.onStartupStateChanged(startupState)
        }
    }

    fun removeObserver(observer: StartupStateObserver) {
        observers.remove(observer)
        DebugLogger.info(tag, "Remove observer $observer. Total count = ${observers.size}")
    }
}
