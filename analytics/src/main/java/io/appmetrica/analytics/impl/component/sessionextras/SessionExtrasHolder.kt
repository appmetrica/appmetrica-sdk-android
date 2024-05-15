package io.appmetrica.analytics.impl.component.sessionextras

import android.content.Context
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.logger.internal.DebugLogger

internal class SessionExtrasHolder(context: Context, componentId: ComponentId) {

    private val tag = "[SessionExtrasHolder-${componentId.apiKey?.let { Utils.createPartialApiKey(it) }}]"

    private val storage = SessionExtrasStorage(context, componentId)

    private val state = storage.extras.toMutableMap().also {
        DebugLogger.info(tag, "Initial session extras state: $it")
    }

    val snapshot: Map<String, ByteArray>
        get() = HashMap(state).also { DebugLogger.info(tag, "snapshot = %s", it) }

    fun put(key: String, value: ByteArray?) {
        if (value == null || value.isEmpty()) {
            val prev = state.remove(key)
            DebugLogger.info(tag, "Remove extra for key = `$key`; prev stored value size = ${prev?.size}")
        } else {
            val prev = state.put(key, value)
            DebugLogger.info(tag, "Update extra for key = `$key`; prev stored value size = ${prev?.size}")
        }
        DebugLogger.info(tag, "Save new session extras state: $state")
        storage.extras = state
    }
}
