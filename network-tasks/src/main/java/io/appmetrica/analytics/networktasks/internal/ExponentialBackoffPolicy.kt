package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.networktasks.internal.attempt.AttemptOutcome

interface ExponentialBackoffPolicy {

    fun onHostAttemptFinished(attemptOutcome: AttemptOutcome)

    fun onAllHostsAttemptsFinished(attemptOutcome: AttemptOutcome)

    fun canBeExecuted(retryPolicyConfig: RetryPolicyConfig?): Boolean
}
