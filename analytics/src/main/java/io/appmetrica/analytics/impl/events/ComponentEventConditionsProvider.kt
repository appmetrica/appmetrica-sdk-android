package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.DatabaseHelper

class ComponentEventConditionsProvider(
    private val databaseHelper: DatabaseHelper,
    private val configurationHolder: ReportComponentConfigurationHolder,
) : EventConditionsProvider {

    override fun getCommonEventConditions(): List<EventCondition> = listOf(
        ContainsUrgentEventsCondition(databaseHelper),
        MaxReportsCountReachedCondition(databaseHelper, configurationHolder)
    )

    override fun getForceSendEventConditions(): List<EventCondition> = emptyList()
}
