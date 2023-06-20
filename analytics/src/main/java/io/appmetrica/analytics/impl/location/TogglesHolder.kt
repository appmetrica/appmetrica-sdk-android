package io.appmetrica.analytics.impl.location

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreutils.internal.toggle.ConjunctiveCompositeThreadSafeToggle
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.location.toggles.ClientApiTrackingStatusToggle
import io.appmetrica.analytics.impl.location.toggles.VisibleAppStateOnlyTrackingStatusToggle
import io.appmetrica.analytics.impl.location.toggles.WakelocksToggle

internal class TogglesHolder(
    appStateToggle: Toggle?
) {
    val clientTrackingStatusController = ClientApiTrackingStatusToggle(
        GlobalServiceLocator.getInstance().servicePreferences
    )
    val wakelocksToggle = WakelocksToggle()

    val resultLocationControlToggle = ConjunctiveCompositeThreadSafeToggle(
        listOf(
            clientTrackingStatusController,
            wakelocksToggle,
            appStateToggle ?: VisibleAppStateOnlyTrackingStatusToggle()
        ),
        "loc-def"
    )
}
