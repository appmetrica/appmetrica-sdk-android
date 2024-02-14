package io.appmetrica.analytics.impl.location

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.control.ToggleObserver
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.locationapi.internal.LocationControllerObserver
import io.appmetrica.analytics.logger.internal.YLogger

private const val TAG = "[LocationControllerImpl]"

internal class LocationControllerImpl : LocationController, ToggleObserver {

    private val observers = ArrayList<LocationControllerObserver>()
    private val executor = GlobalServiceLocator.getInstance().serviceExecutorProvider.moduleExecutor
    private lateinit var togglesHolder: TogglesHolder

    private var activated = false

    override fun init(outerAppStateToggle: Toggle?) {
        YLogger.info(TAG, "Init")
        togglesHolder = TogglesHolder(outerAppStateToggle)
        togglesHolder.resultLocationControlToggle.registerObserver(this, true)
    }

    override fun registerObserver(observer: LocationControllerObserver, sticky: Boolean) {
        YLogger.info(TAG, "registerObserver: $observer")
        executor.execute {
            observers.add(observer)
            if (sticky) {
                YLogger.info(TAG, "Notify observer $observer as sticky - activated = $activated")
                if (activated) {
                    observer.startLocationTracking()
                } else {
                    observer.stopLocationTracking()
                }
            }
        }
    }

    override fun onStateChanged(incomingState: Boolean) {
        YLogger.info(TAG, "onStateChanged = $incomingState")
        executor.execute {
            if (activated != incomingState) {
                YLogger.info(TAG, "State changed from $activated to $incomingState")
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
