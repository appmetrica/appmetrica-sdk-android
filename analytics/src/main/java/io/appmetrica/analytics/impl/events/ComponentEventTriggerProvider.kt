package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.DatabaseHelper

internal class ComponentEventTriggerProvider(
    eventsFlusher: EventsFlusher,
    databaseHelper: DatabaseHelper,
    configurationHolder: ReportComponentConfigurationHolder,
    componentId: ComponentId
) : EventTriggerProvider {

    private val eventConditionsProvider = ComponentEventConditionsProvider(
        databaseHelper,
        configurationHolder
    )

    override val eventTrigger: ConditionalEventTrigger =
        ConditionalEventTrigger(
            eventsFlusher,
            eventConditionsProvider.getCommonEventConditions(),
            eventConditionsProvider.getForceSendEventConditions(),
            componentId
        )
}
