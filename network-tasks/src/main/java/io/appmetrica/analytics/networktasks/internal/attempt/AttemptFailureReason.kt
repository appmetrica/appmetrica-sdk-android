package io.appmetrica.analytics.networktasks.internal.attempt

/** Task-side classification of an attempt failure. */
enum class AttemptFailureReason {
    /** No specific classification (also used when there is no failure). */
    UNCLASSIFIED,

    /** Endpoint rejected the payload (e.g. HTTP 400). */
    PAYLOAD_REJECTED,
}
