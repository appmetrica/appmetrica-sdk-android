package io.appmetrica.analytics.impl

import android.os.Bundle
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger

/**
 * Flat CoreClientEvent fields carried over IPC (see [EventIpcBundleKeys]).
 * Used by [EventIpcCodec] and mapped to [CoreServiceEvent] on the service.
 */
internal class EventIpcData(
    val name: String? = null,
    val value: ByteArray? = null,
    val type: Int = -1,
    val customType: Int = -1,
    val bytesTruncated: Int = 0,
    val profileID: String? = null,
    val eventEnvironment: String? = null,
    val creationElapsedRealtime: Long = 0L,
    val creationTimestamp: Long = 0L,
    val source: EventSource? = null,
    val payload: Bundle? = null,
    val extras: MutableMap<String, ByteArray> = HashMap(),
    val valueProtocolVersion: Int? = null,
) {

    companion object {

        @JvmStatic
        fun fromCoreClientEvent(logger: PublicLogger, report: CoreClientEvent): EventIpcData {
            return from(TrimmedCoreClientEvent(logger, report))
        }

        @JvmStatic
        fun from(trimmed: TrimmedCoreClientEvent): EventIpcData {
            return EventIpcData(
                name = trimmed.name,
                value = trimmed.valueBytes,
                type = trimmed.type,
                customType = trimmed.customType,
                bytesTruncated = trimmed.bytesTruncated,
                profileID = trimmed.profileID,
                eventEnvironment = trimmed.eventEnvironment,
                creationElapsedRealtime = trimmed.creationElapsedRealtime,
                creationTimestamp = trimmed.creationTimestamp,
                source = trimmed.source,
                payload = trimmed.payload,
                extras = HashMap(trimmed.extras),
                valueProtocolVersion = trimmed.valueProtocolVersion,
            )
        }
    }
}
