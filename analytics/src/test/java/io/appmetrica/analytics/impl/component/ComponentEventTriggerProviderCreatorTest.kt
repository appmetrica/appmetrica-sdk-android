package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.events.ComponentEventTriggerProvider
import io.appmetrica.analytics.impl.events.EventsFlusher
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

internal class ComponentEventTriggerProviderCreatorTest : CommonTest() {

    private val eventsFlusher: EventsFlusher = mock()
    private val databaseHelper: DatabaseHelper = mock()
    private val configurationHolder: ReportComponentConfigurationHolder = mock()
    private val initialConfig: CommonArguments.ReporterArguments = mock()
    private val componentId: ComponentId = mock()
    private val preferences: PreferencesComponentDbStorage = mock()

    @get:Rule
    val componentEventTriggerProviderConstructionRule = constructionRule<ComponentEventTriggerProvider>()

    private val componentEventProviderCreator: ComponentEventTriggerProviderCreator by setUp {
        ComponentEventTriggerProviderCreator()
    }

    @Test
    fun createEventTriggerProvider() {
        assertThat(componentEventProviderCreator.createEventTriggerProvider(
            eventsFlusher,
            databaseHelper,
            configurationHolder,
            initialConfig,
            componentId,
            preferences
        )).isEqualTo(componentEventTriggerProviderConstructionRule.constructionMock.constructed().first())
        assertThat(componentEventTriggerProviderConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(componentEventTriggerProviderConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(eventsFlusher, databaseHelper, configurationHolder, componentId)
    }
}
