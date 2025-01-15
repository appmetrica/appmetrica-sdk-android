package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.events.ComponentEventTriggerProvider
import io.appmetrica.analytics.impl.events.EventTriggerProvider
import io.appmetrica.analytics.impl.events.EventsFlusher

internal class ComponentEventTriggerProviderCreator : EventTriggerProviderCreator {

    override fun createEventTriggerProvider(
        eventFlusher: EventsFlusher,
        databaseHelper: DatabaseHelper,
        configurationHolder: ReportComponentConfigurationHolder,
        initialConfig: CommonArguments.ReporterArguments,
        componentId: ComponentId,
        preferences: PreferencesComponentDbStorage
    ): EventTriggerProvider = ComponentEventTriggerProvider(
        eventFlusher,
        databaseHelper,
        configurationHolder,
        componentId
    )
}
