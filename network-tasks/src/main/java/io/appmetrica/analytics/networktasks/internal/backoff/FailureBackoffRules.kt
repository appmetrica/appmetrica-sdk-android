package io.appmetrica.analytics.networktasks.internal.backoff

import io.appmetrica.analytics.networktasks.internal.attempt.AttemptOutcome

/** Maps [AttemptOutcome] to [BackoffAction]; chosen when assembling the network task. */
fun interface FailureBackoffRules {

    fun backoffAction(outcome: AttemptOutcome): BackoffAction
}
