package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class AllHostsExponentialBackoffPolicy(
    private val exponentialBackoffDataHolder: ExponentialBackoffDataHolder
) : ExponentialBackoffPolicy {

    private val tag = "[AllHostsExponentialBackoffPolicy]"

    override fun onHostAttemptFinished(success: Boolean) {
        // do nothing
    }

    override fun onAllHostsAttemptsFinished(success: Boolean) {
        DebugLogger.info(tag, "onAllHostsAttemptsFinished with success = %b", success)
        if (success) {
            exponentialBackoffDataHolder.reset()
        } else {
            exponentialBackoffDataHolder.updateLastAttemptInfo()
        }
    }

    override fun canBeExecuted(retryPolicyConfig: RetryPolicyConfig?): Boolean {
        return exponentialBackoffDataHolder.wasLastAttemptLongAgoEnough(retryPolicyConfig)
    }
}
