package io.appmetrica.analytics.impl

internal enum class ClientReportsQueueDropReason {
    /** New DATA item alone exceeds the DATA budget limit. */
    EVENT_EXCEEDS_BUDGET,

    /** After eviction attempts the new DATA item still does not fit. */
    QUEUE_BUDGET_EXCEEDED,

    /** Oldest pending non-started DATA was removed to free space. */
    EVICTED_OLDEST,
}
