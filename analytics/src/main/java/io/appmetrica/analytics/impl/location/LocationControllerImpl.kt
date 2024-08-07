package io.appmetrica.analytics.impl.location

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.control.ToggleObserver
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.locationapi.internal.LocationControllerObserver
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class LocationControllerImpl : LocationController, ToggleObserver {

    private val tag = "[LocationControllerImpl]"

    private val observers = ArrayList<LocationControllerObserver>()
    private val executor = GlobalServiceLocator.getInstance().serviceExecutorProvider.moduleExecutor
    private lateinit var togglesHolder: TogglesHolder

    private var activated = false

    override fun init(outerAppStateToggle: Toggle?) {
        DebugLogger.info(tag, "Init")
        togglesHolder = TogglesHolder(outerAppStateToggle)
        togglesHolder.resultLocationControlToggle.registerObserver(this, true)
    }

    override fun registerObserver(observer: LocationControllerObserver, sticky: Boolean) {
        DebugLogger.info(tag, "registerObserver: $observer")
        executor.execute {
            observers.add(observer)
            if (sticky) {
                DebugLogger.info(tag, "Notify observer $observer as sticky - activated = $activated")
                if (activated) {
                    observer.startLocationTracking()
                } else {
                    observer.stopLocationTracking()
                }
            }
        }
    }

    override fun onStateChanged(incomingState: Boolean) {
        DebugLogger.info(tag, "onStateChanged = $incomingState")
        executor.execute {
            if (activated != incomingState) {
                DebugLogger.info(tag, "State changed from $activated to $incomingState")
                activated = incomingState
                val api = if (activated) {
                    { locationControllerObserver: LocationControllerObserver ->
                        locationControllerObserver.startLocationTracking()
                    }
                } else {
                    { locationControllerObserver: LocationControllerObserver ->
                        locationControllerObserver.stopLocationTracking()
                    }
                }
                observers.forEach {
                    api(it)
                }
            }
        }
    }

    override fun registerWakelock(wakelock: Any) {
        togglesHolder.wakelocksToggle.registerWakelock(wakelock)
    }

    override fun removeWakelock(wakelock: Any) {
        togglesHolder.wakelocksToggle.removeWakelock(wakelock)
    }

    override fun updateTrackingStatusFromClient(value: Boolean) {
        togglesHolder.clientTrackingStatusController.updateTrackingStatus(value)
    }
}
