package io.appmetrica.analytics.impl.db.event

import android.util.Base64
import io.appmetrica.analytics.impl.InternalEvents
import java.nio.charset.StandardCharsets

/**
 * Encodes in-memory event value bytes into the legacy DB string form.
 *
 * Mirrors [io.appmetrica.analytics.impl.ProtobufUtils] value composers:
 * text events stay as UTF-8 strings; binary events are Base64-encoded.
 * Native crash dumps remain Base64 text in memory (via setValue), so they
 * are written as UTF-8 without a second Base64 pass.
 */
internal object DbEventValueEncoder {

    private val BINARY_EVENT_TYPES = setOf(
        InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF,
        InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF,
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF,
        InternalEvents.EVENT_TYPE_PREV_SESSION_EXCEPTION_UNHANDLED_FROM_FILE,
        InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE,
        InternalEvents.EVENT_TYPE_ANR,
        InternalEvents.EVENT_TYPE_START,
        InternalEvents.EVENT_TYPE_SEND_USER_PROFILE,
        InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT,
        InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT,
        InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT,
        InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION,
        InternalEvents.EVENT_TYPE_SEND_REFERRER,
    )

    @JvmStatic
    fun encode(valueBytes: ByteArray?, type: Int, valueProtocolVersion: Int?): String? {
        if (valueBytes == null) {
            return null
        }
        val eventType = InternalEvents.valueOf(type)
        return if (shouldBase64Encode(eventType, valueProtocolVersion)) {
            String(Base64.encode(valueBytes, Base64.DEFAULT), StandardCharsets.UTF_8)
        } else {
            String(valueBytes, StandardCharsets.UTF_8)
        }
    }

    private fun shouldBase64Encode(
        eventType: InternalEvents,
        valueProtocolVersion: Int?
    ): Boolean {
        if (eventType == InternalEvents.EVENT_TYPE_CUSTOM_EVENT) {
            return (valueProtocolVersion ?: 0) >= 2
        }
        return eventType in BINARY_EVENT_TYPES
    }
}
