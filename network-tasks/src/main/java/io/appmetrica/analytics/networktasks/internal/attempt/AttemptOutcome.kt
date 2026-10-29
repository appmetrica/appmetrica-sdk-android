package io.appmetrica.analytics.networktasks.internal.attempt

/** Result of scheduling or performing one network-task execution cycle. */
sealed class AttemptOutcome {
    abstract val retry: RetryState

    /** Seed for the host loop: no attempt yet, perform the first request. */
    data object ReadyToRun : AttemptOutcome() {
        override val retry: RetryState = RetryState.RUN
    }

    /** Task was not executed (e.g. nothing to send). */
    data object Skipped : AttemptOutcome() {
        override val retry: RetryState = RetryState.STOP
    }

    /** A network attempt (or an equivalent strategy step) has completed. */
    data class Completed(
        val succeeded: Boolean,
        override val retry: RetryState,
        val failureReason: AttemptFailureReason,
    ) : AttemptOutcome()
}
