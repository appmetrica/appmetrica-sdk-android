package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.events.EventTriggerProvider
import io.appmetrica.analytics.impl.events.EventsFlusher
import io.appmetrica.analytics.impl.events.MainComponentEventTriggerProvider

internal class MainComponentEventTriggerProviderCreator : EventTriggerProviderCreator {

    override fun createEventTriggerProvider(
        eventFlusher: EventsFlusher,
        databaseHelper: DatabaseHelper,
        configurationHolder: ReportComponentConfigurationHolder,
        initialConfig: CommonArguments.ReporterArguments,
        componentId: ComponentId,
        preferences: PreferencesComponentDbStorage
    ): EventTriggerProvider = MainComponentEventTriggerProvider(
        eventFlusher,
        databaseHelper,
        configurationHolder,
        initialConfig,
        componentId,
        preferences
    )
}
