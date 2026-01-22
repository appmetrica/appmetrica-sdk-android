package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage

internal class MainComponentEventConditionsProvider(
    databaseHelper: DatabaseHelper,
    private val configurationHolder: ReportComponentConfigurationHolder,
    private val eventTriggerProvider: EventTriggerProvider,
    private val initialConfig: CommonArguments.ReporterArguments,
    private val preferences: PreferencesComponentDbStorage
) : EventConditionsProvider {

    private val componentEventConditionsProvider = ComponentEventConditionsProvider(
        databaseHelper,
        configurationHolder
    )

    private val mainReporterEventSendingPolicyProvider = MainReporterEventSendingPolicyProvider()

    private val mainReporterPolicyCondition: EventCondition by lazy {
        mainReporterEventSendingPolicyProvider.getPolicy(
            eventTriggerProvider,
            configurationHolder,
            initialConfig,
            preferences
        ).condition
    }

    override fun getCommonEventConditions(): List<EventCondition> {
        return componentEventConditionsProvider.getCommonEventConditions()
    }

    override fun getForceSendEventConditions(): List<EventCondition> =
        componentEventConditionsProvider.getForceSendEventConditions() + listOf(mainReporterPolicyCondition)
}
