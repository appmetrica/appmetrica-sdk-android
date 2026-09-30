package io.appmetrica.analytics.impl

import android.os.Bundle
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.impl.utils.limitation.EventFieldTrimmer
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import java.nio.charset.StandardCharsets
import java.util.Arrays

internal class TrimmedCoreClientEvent(
    logger: PublicLogger,
    event: CoreClientEvent,
) {
    val type: Int = event.type
    val customType: Int = event.customType
    val valueProtocolVersion: Int? = event.valueProtocolVersion
    val extras: Map<String, ByteArray> = event.extras
    val eventEnvironment: String? = event.eventEnvironment
    val creationElapsedRealtime: Long = event.creationElapsedRealtime
    val creationTimestamp: Long = event.creationTimestamp
    val source: EventSource? = event.source
    val payload: Bundle? = event.payload

    val name: String? = EventFieldTrimmer.nameTrimmer(logger, event.trimPolicy).trim(event.name)
    val valueBytes: ByteArray? =
        EventFieldTrimmer.valueBytesTrimmer(logger, event.trimPolicy).trim(event.valueBytes)
    val profileID: String? =
        EventFieldTrimmer.profileIdTrimmer(logger, event.trimPolicy).trim(event.profileID)
    val value: String? = valueBytes?.let { String(it, StandardCharsets.UTF_8) }

    val bytesTruncated: Int =
        event.bytesTruncated +
            nameDeltaBytes(event.name, name) +
            valueDeltaBytes(event.valueBytes, valueBytes)

    override fun toString(): String {
        return "[" +
            "event: $name, " +
            "type: ${InternalEvents.valueOf(type).info}, " +
            "value: ${Utils.trimToSize(value, Limits.EVENT_VALUE_FOR_LOGS_LIMIT)}" +
            "]"
    }

    private fun nameDeltaBytes(original: String?, trimmed: String?): Int {
        if (!EventLimitationProcessor.valueWasTrimmed(original, trimmed)) {
            return 0
        }
        return StringUtils.getUTF8Bytes(original).size - StringUtils.getUTF8Bytes(trimmed).size
    }

    private fun valueDeltaBytes(original: ByteArray?, trimmed: ByteArray?): Int {
        if (Arrays.equals(original, trimmed)) {
            return 0
        }
        return (original?.size ?: 0) - (trimmed?.size ?: 0)
    }
}
