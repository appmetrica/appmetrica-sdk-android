package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.networktasks.internal.attempt.AttemptOutcome
import io.appmetrica.analytics.networktasks.internal.backoff.AlwaysUpdateOnFailure
import io.appmetrica.analytics.networktasks.internal.backoff.BackoffAction
import io.appmetrica.analytics.networktasks.internal.backoff.FailureBackoffRules

class AllHostsExponentialBackoffPolicy @JvmOverloads constructor(
    private val exponentialBackoffDataHolder: ExponentialBackoffDataHolder,
    private val failureBackoffRules: FailureBackoffRules = AlwaysUpdateOnFailure,
) : ExponentialBackoffPolicy {

    private val tag = "[AllHostsExponentialBackoffPolicy]"

    override fun onHostAttemptFinished(attemptOutcome: AttemptOutcome) {
        // do nothing
    }

    override fun onAllHostsAttemptsFinished(attemptOutcome: AttemptOutcome) {
        val backoffAction = failureBackoffRules.backoffAction(attemptOutcome)
        DebugLogger.info(
            tag,
            "onAllHostsAttemptsFinished with backoffAction = $backoffAction, outcome = $attemptOutcome",
        )
        when (backoffAction) {
            BackoffAction.RESET -> exponentialBackoffDataHolder.reset()
            BackoffAction.UPDATE -> exponentialBackoffDataHolder.updateLastAttemptInfo()
            BackoffAction.DO_NOTHING -> {
                // do nothing
            }
        }
    }

    override fun canBeExecuted(retryPolicyConfig: RetryPolicyConfig?): Boolean {
        return exponentialBackoffDataHolder.wasLastAttemptLongAgoEnough(retryPolicyConfig)
    }
}
