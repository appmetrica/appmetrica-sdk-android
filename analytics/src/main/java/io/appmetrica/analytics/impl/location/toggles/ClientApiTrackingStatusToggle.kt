package io.appmetrica.analytics.impl.location.toggles

import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.coreutils.internal.toggle.SimpleThreadSafeToggle
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.impl.location.ClientTrackingStatusController

class ClientApiTrackingStatusToggle(
    private val storage: PreferencesServiceDbStorage
) : SimpleThreadSafeToggle(
    initialState = storage.isLocationTrackingEnabled,
    tag = "[ClientApiTrackingStatusToggle]"
),
    ClientTrackingStatusController {

    override fun updateTrackingStatus(status: Boolean) {
        YLogger.info(tag, "update tracking status to `$status`")
        updateState(status)
        storage.saveLocationTrackingEnabled(status)
    }
}
