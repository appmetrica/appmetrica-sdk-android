package io.appmetrica.analytics.impl

import android.os.Bundle

/**
 * Flat CounterReport fields carried over IPC (see [EventIpcBundleKeys]).
 * Used by [EventIpcCodec] and mapped to [CoreServiceEvent] / DiagnosticEvent on the service.
 */
internal class EventIpcData(
    val name: String? = null,
    val value: String? = null,
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
        fun fromCounterReport(report: CounterReport): EventIpcData {
            return EventIpcData(
                name = report.name,
                value = report.value,
                type = report.type,
                customType = report.customType,
                bytesTruncated = report.bytesTruncated,
                profileID = report.profileID,
                eventEnvironment = report.eventEnvironment,
                creationElapsedRealtime = report.creationElapsedRealtime,
                creationTimestamp = report.creationTimestamp,
                source = report.source,
                payload = report.payload,
                extras = HashMap(report.extras),
                valueProtocolVersion = report.valueProtocolVersion,
            )
        }
    }
}
