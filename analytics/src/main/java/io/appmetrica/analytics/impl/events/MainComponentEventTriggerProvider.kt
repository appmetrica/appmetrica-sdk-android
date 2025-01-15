package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage

internal class MainComponentEventTriggerProvider(
    eventsFlusher: EventsFlusher,
    databaseHelper: DatabaseHelper,
    configurationHolder: ReportComponentConfigurationHolder,
    initialConfig: CommonArguments.ReporterArguments,
    componentId: ComponentId,
    preferences: PreferencesComponentDbStorage
) : EventTriggerProvider {

    private val eventConditionsProvider = MainComponentEventConditionsProvider(
        databaseHelper,
        configurationHolder,
        this,
        initialConfig,
        preferences
    )

    override val eventTrigger: ConditionalEventTrigger = ConditionalEventTrigger(
        eventsFlusher,
        eventConditionsProvider.getCommonEventConditions(),
        eventConditionsProvider.getForceSendEventConditions(),
        componentId
    )
}
