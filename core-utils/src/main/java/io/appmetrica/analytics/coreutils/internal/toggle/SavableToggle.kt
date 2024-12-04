package io.appmetrica.analytics.coreutils.internal.toggle

import io.appmetrica.analytics.coreapi.internal.data.Savable
import io.appmetrica.analytics.coreapi.internal.data.Updatable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class SavableToggle(
    subTag: String,
    private val savable: Savable<Boolean>
) : SimpleThreadSafeToggle(
    initialState = savable.value,
    tag = "[SavableToggle - $subTag]"
),
    Updatable<Boolean> {

    override fun update(value: Boolean) {
        DebugLogger.info(tag, "update state: $actualState -> $value")
        updateState(value)
        savable.value = actualState
    }
}
