package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MainComponentEventTriggerProviderTest : CommonTest() {

    private val eventsFlusher: EventsFlusher = mock()
    private val databaseHelper: DatabaseHelper = mock()
    private val configurationHolder: ReportComponentConfigurationHolder = mock()
    private val initialConfig: CommonArguments.ReporterArguments = mock()
    private val componentId: ComponentId = mock()
    private val preferences: PreferencesComponentDbStorage = mock()

    private val commonConditions = listOf(mock<EventCondition>(), mock<EventCondition>())
    private val forceSendConditions = listOf(mock<EventCondition>(), mock<EventCondition>())

    @get:Rule
    val mainComponentEventConditionsProviderRule = constructionRule<MainComponentEventConditionsProvider> {
        on { getCommonEventConditions() } doReturn commonConditions
        on { getForceSendEventConditions() } doReturn forceSendConditions
    }

    @get:Rule
    val conditionEventTriggerRule = constructionRule<ConditionalEventTrigger>()

    private val mainComponentEventTriggerProvider: MainComponentEventTriggerProvider by setUp {
        MainComponentEventTriggerProvider(
            eventsFlusher,
            databaseHelper,
            configurationHolder,
            initialConfig,
            componentId,
            preferences
        )
    }

    @Test
    fun eventTrigger() {
        assertThat(mainComponentEventTriggerProvider.eventTrigger)
            .isEqualTo(conditionEventTriggerRule.constructionMock.constructed().first())
        assertThat(conditionEventTriggerRule.constructionMock.constructed()).hasSize(1)
        assertThat(conditionEventTriggerRule.argumentInterceptor.flatArguments())
            .containsExactly(eventsFlusher, commonConditions, forceSendConditions, componentId)
    }

    @Test
    fun eventConditionProvider() {
        assertThat(mainComponentEventConditionsProviderRule.constructionMock.constructed()).hasSize(1)
        assertThat(mainComponentEventConditionsProviderRule.argumentInterceptor.flatArguments())
            .containsExactly(
                databaseHelper,
                configurationHolder,
                mainComponentEventTriggerProvider,
                initialConfig,
                preferences
            )
    }
}
