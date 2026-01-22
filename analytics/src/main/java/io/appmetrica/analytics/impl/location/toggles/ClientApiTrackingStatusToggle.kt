package io.appmetrica.analytics.impl.location.toggles

import io.appmetrica.analytics.coreutils.internal.toggle.SimpleThreadSafeToggle
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.impl.location.ClientTrackingStatusController
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ClientApiTrackingStatusToggle(
    private val storage: PreferencesServiceDbStorage
) : SimpleThreadSafeToggle(
    initialState = storage.isLocationTrackingEnabled,
    tag = "[ClientApiTrackingStatusToggle]"
),
    ClientTrackingStatusController {

    override fun updateTrackingStatus(status: Boolean) {
        DebugLogger.info(tag, "update tracking status to `$status`")
        updateState(status)
        storage.saveLocationTrackingEnabled(status)
    }
}
