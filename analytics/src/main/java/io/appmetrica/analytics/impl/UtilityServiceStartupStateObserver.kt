package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceConfiguration
import io.appmetrica.analytics.coreutils.internal.services.UtilityServiceProvider
import io.appmetrica.analytics.impl.startup.StartupState

internal class UtilityServiceStartupStateObserver(
    private val utilities: UtilityServiceProvider
) : StartupStateObserver {
    override fun onStartupStateChanged(startupState: StartupState) {
        utilities.updateConfiguration(
            UtilityServiceConfiguration(startupState.firstStartupServerTime, startupState.obtainServerTime)
        )
    }
}
