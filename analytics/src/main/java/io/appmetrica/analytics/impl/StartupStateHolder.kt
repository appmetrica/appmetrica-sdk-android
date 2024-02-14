package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.logger.internal.YLogger
import java.util.concurrent.CopyOnWriteArrayList

private const val TAG = "[StartupStateHolder]"
internal class StartupStateHolder : StartupStateObserver {

    @Volatile
    private lateinit var startupState: StartupState

    private val observers = CopyOnWriteArrayList<StartupStateObserver>()

    fun init(context: Context) {
        YLogger.info(TAG, "Init")
        onStartupStateChanged(StartupState.Storage(context).read())
    }

    override fun onStartupStateChanged(startupState: StartupState) {
        YLogger.info(TAG, "onStartupStateChanged. Observers count = ${observers.size}")
        this.startupState = startupState
        observers.forEach { it.onStartupStateChanged(startupState) }
    }

    fun getStartupState(): StartupState = startupState

    fun registerObserver(observer: StartupStateObserver) {
        observers.add(observer)
        YLogger.info(TAG, "Register observer $observer. Total count = ${observers.size}")
        if (this::startupState.isInitialized) {
            observer.onStartupStateChanged(startupState)
        }
    }

    fun removeObserver(observer: StartupStateObserver) {
        observers.remove(observer)
        YLogger.info(TAG, "Remove observer $observer. Total count = ${observers.size}")
    }
}
