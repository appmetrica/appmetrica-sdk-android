package io.appmetrica.analytics.networktasks.internal.backoff

import io.appmetrica.analytics.networktasks.internal.attempt.AttemptOutcome

object AlwaysUpdateOnFailure : FailureBackoffRules {
    override fun backoffAction(outcome: AttemptOutcome): BackoffAction = when (outcome) {
        is AttemptOutcome.ReadyToRun,
        is AttemptOutcome.Skipped -> BackoffAction.DO_NOTHING

        is AttemptOutcome.Completed -> if (outcome.succeeded) {
            BackoffAction.RESET
        } else {
            BackoffAction.UPDATE
        }
    }
}
