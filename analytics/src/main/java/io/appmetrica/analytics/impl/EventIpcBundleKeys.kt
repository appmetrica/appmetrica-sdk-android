package io.appmetrica.analytics.impl

/**
 * Flat Bundle keys for [EventIpcData] IPC.
 * Wire string values keep the historical `CounterReport.` prefix for compatibility
 * with the former nested Parcelable Bundle layout.
 */
internal object EventIpcBundleKeys {
    private const val PREFIX = "AppMetrica.EventIpc."

    const val EVENT = PREFIX + "Event"
    const val TYPE = PREFIX + "Type"
    const val CUSTOM_TYPE = PREFIX + "CustomType"
    const val VALUE = PREFIX + "Value"
    const val ENVIRONMENT = PREFIX + "Environment"
    const val TRUNCATED = PREFIX + "TRUNCATED"
    const val PROFILE_ID = PREFIX + "ProfileID"
    const val CREATION_ELAPSED_REALTIME = PREFIX + "CreationElapsedRealtime"
    const val CREATION_TIMESTAMP = PREFIX + "CreationTimestamp"
    const val SOURCE = PREFIX + "Source"
    const val PAYLOAD = PREFIX + "Payload"
    const val EXTRAS = PREFIX + "Extras"
    const val VALUE_PROTOCOL_VERSION = PREFIX + "ValueProtocolVersion"
}
