package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.events.EventTriggerProvider
import io.appmetrica.analytics.impl.events.EventsFlusher

internal interface EventTriggerProviderCreator {

    fun createEventTriggerProvider(
        eventFlusher: EventsFlusher,
        databaseHelper: DatabaseHelper,
        configurationHolder: ReportComponentConfigurationHolder,
        initialConfig: CommonArguments.ReporterArguments,
        componentId: ComponentId,
        preferences: PreferencesComponentDbStorage
    ): EventTriggerProvider
}
