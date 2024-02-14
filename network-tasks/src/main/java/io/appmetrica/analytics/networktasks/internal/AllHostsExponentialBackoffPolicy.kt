package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.logger.internal.YLogger

private const val TAG = "[AllHostsExponentialBackoffPolicy]"

class AllHostsExponentialBackoffPolicy(
    private val exponentialBackoffDataHolder: ExponentialBackoffDataHolder
) : ExponentialBackoffPolicy {

    override fun onHostAttemptFinished(success: Boolean) {
        // do nothing
    }

    override fun onAllHostsAttemptsFinished(success: Boolean) {
        YLogger.info(TAG, "onAllHostsAttemptsFinished with success = %b", success)
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
