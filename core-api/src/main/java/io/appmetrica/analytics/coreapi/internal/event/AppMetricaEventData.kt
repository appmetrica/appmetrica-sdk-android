package io.appmetrica.analytics.coreapi.internal.event

/**
 * Internal typed custom event. Binary schema is defined by the module implementation.
 */
abstract class AppMetricaEventData {

    /**
     * Human-readable description of the event.
     */
    abstract val description: String?

    /**
     * Custom type id.
     */
    abstract val type: Int

    /**
     * Event name.
     */
    open val name: String? = null

    /**
     * Opaque binary payload. Must not be null or empty — validated by SDK barrier.
     */
    abstract val data: ByteArray

    /**
     * Number of bytes truncated by the module before passing data to SDK.
     * Added to the SDK's own truncation count in `CounterReport.bytesTruncated`.
     */
    abstract val bytesTruncated: Int
}
