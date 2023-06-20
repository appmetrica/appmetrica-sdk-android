package io.appmetrica.analytics.impl.location.toggles

import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateObserver
import io.appmetrica.analytics.coreutils.internal.toggle.SimpleThreadSafeToggle
import io.appmetrica.analytics.impl.GlobalServiceLocator

class VisibleAppStateOnlyTrackingStatusToggle :
    SimpleThreadSafeToggle(
        false,
        "[VisibleAppStateOnlyTrackingStatusToggle]"
    ),
    ApplicationStateObserver {

    init {
        synchronized(this) {
            handleAppState(GlobalServiceLocator.getInstance().applicationStateProvider.registerStickyObserver(this))
        }
    }

    @Synchronized
    override fun onApplicationStateChanged(applicationState: ApplicationState) {
        handleAppState(applicationState)
    }

    private fun handleAppState(applicationState: ApplicationState) {
        updateState(applicationState == ApplicationState.VISIBLE)
    }
}
