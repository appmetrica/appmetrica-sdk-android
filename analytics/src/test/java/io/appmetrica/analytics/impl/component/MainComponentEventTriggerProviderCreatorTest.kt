package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.events.EventsFlusher
import io.appmetrica.analytics.impl.events.MainComponentEventTriggerProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

internal class MainComponentEventTriggerProviderCreatorTest : CommonTest() {

    private val eventsFlusher: EventsFlusher = mock()
    private val databaseHelper: DatabaseHelper = mock()
    private val configurationHolder: ReportComponentConfigurationHolder = mock()
    private val initialConfig: CommonArguments.ReporterArguments = mock()
    private val componentId: ComponentId = mock()
    private val preferences: PreferencesComponentDbStorage = mock()

    @get:Rule
    val mainComponentEventTriggerProviderRule = constructionRule<MainComponentEventTriggerProvider>()

    private val creator: MainComponentEventTriggerProviderCreator by setUp {
        MainComponentEventTriggerProviderCreator()
    }

    @Test
    fun createEventTriggerProvider() {
        assertThat(creator.createEventTriggerProvider(
            eventsFlusher,
            databaseHelper,
            configurationHolder,
            initialConfig,
            componentId,
            preferences
        )).isEqualTo(mainComponentEventTriggerProviderRule.constructionMock.constructed().first())
        assertThat(mainComponentEventTriggerProviderRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainComponentEventTriggerProviderRule.argumentInterceptor.flatArguments())
            .containsExactly(
                eventsFlusher,
                databaseHelper,
                configurationHolder,
                initialConfig,
                componentId,
                preferences
            )
    }
}
