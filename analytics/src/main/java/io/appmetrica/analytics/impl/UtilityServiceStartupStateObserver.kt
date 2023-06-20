package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceConfiguration
import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceLocator
import io.appmetrica.analytics.impl.startup.StartupState

internal class UtilityServiceStartupStateObserver : StartupStateObserver {
    override fun onStartupStateChanged(startupState: StartupState) {
        UtilityServiceLocator.instance.updateConfiguration(
            UtilityServiceConfiguration(startupState.firstStartupServerTime, startupState.obtainServerTime)
        )
    }
}
