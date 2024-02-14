package io.appmetrica.analytics.impl.component.sessionextras

import android.content.Context
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.logger.internal.YLogger

internal class SessionExtrasHolder(context: Context, componentId: ComponentId) {

    private val tag = "[SessionExtrasHolder-${componentId.apiKey?.let { Utils.createPartialApiKey(it) }}]"

    private val storage = SessionExtrasStorage(context, componentId)

    private val state = storage.extras.toMutableMap().also {
        YLogger.info(tag, "Initial session extras state: $it")
    }

    val snapshot: Map<String, ByteArray>
        get() = HashMap(state).also { YLogger.info(tag, "snapshot = %s", it) }

    fun put(key: String, value: ByteArray?) {
        if (value == null || value.isEmpty()) {
            val prev = state.remove(key)
            YLogger.info(tag, "Remove extra for key = `$key`; prev stored value size = ${prev?.size}")
        } else {
            val prev = state.put(key, value)
            YLogger.info(tag, "Update extra for key = `$key`; prev stored value size = ${prev?.size}")
        }
        YLogger.info(tag, "Save new session extras state: $state")
        storage.extras = state
    }
}
