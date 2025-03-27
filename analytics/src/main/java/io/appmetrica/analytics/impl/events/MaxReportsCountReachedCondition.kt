package io.appmetrica.analytics.impl.events

class MaxReportsCountReachedCondition(
    private val pendingReportsCountProvider: PendingReportsCountProvider,
    private val threshold: () -> Int
) : EventCondition {

    override fun isConditionMet(): Boolean =
        pendingReportsCountProvider.pendingReportsCount >= threshold()
}
