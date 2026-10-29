package io.appmetrica.analytics.networktasks.internal.backoff

/** How to update shared exponential backoff after host attempts finish. */
enum class BackoffAction {
    /** Leave holder unchanged. */
    DO_NOTHING,

    /** Clear backoff after success. */
    RESET,

    /** Record a failed attempt and increase delay. */
    UPDATE,
}
