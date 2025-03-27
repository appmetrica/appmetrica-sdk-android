package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.DatabaseHelper

class ComponentEventConditionsProvider(
    databaseHelper: DatabaseHelper,
    configurationHolder: ReportComponentConfigurationHolder,
) : EventConditionsProvider {

    private val pendingReportsCountProvider = PendingReportsCountHolder(databaseHelper)
    private val containsUrgentEventsCondition = ContainsUrgentEventsCondition(databaseHelper)

    private val maxReportsCountReachedCondition = MaxReportsCountReachedCondition(pendingReportsCountProvider) {
        configurationHolder.get().maxReportsCount
    }

    private val hasPendingReportsCondition = MaxReportsCountReachedCondition(pendingReportsCountProvider) { 1 }

    override fun getCommonEventConditions(): List<EventCondition> = listOf(
        containsUrgentEventsCondition,
        maxReportsCountReachedCondition
    )

    override fun getForceSendEventConditions(): List<EventCondition> = listOf(hasPendingReportsCondition)
}
