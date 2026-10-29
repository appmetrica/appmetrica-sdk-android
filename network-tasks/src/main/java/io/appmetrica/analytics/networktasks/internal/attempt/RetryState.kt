package io.appmetrica.analytics.networktasks.internal.attempt

/** Whether to try the next host in the same task run. */
enum class RetryState {
    /** Try the next host. */
    RUN,

    /** Stop host attempts. */
    STOP,
}
