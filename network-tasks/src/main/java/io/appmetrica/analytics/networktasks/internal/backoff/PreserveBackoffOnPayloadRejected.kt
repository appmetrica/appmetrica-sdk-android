package io.appmetrica.analytics.networktasks.internal.backoff

import io.appmetrica.analytics.networktasks.internal.attempt.AttemptFailureReason
import io.appmetrica.analytics.networktasks.internal.attempt.AttemptOutcome

object PreserveBackoffOnPayloadRejected : FailureBackoffRules {
    override fun backoffAction(outcome: AttemptOutcome): BackoffAction = when (outcome) {
        is AttemptOutcome.ReadyToRun,
        is AttemptOutcome.Skipped -> BackoffAction.DO_NOTHING

        is AttemptOutcome.Completed -> when {
            outcome.succeeded -> BackoffAction.RESET
            outcome.failureReason == AttemptFailureReason.PAYLOAD_REJECTED -> BackoffAction.DO_NOTHING
            else -> BackoffAction.UPDATE
        }
    }
}
