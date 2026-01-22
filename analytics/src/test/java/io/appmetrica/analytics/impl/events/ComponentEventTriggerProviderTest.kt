package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ComponentEventTriggerProviderTest : CommonTest() {

    private val eventFlusher: EventsFlusher = mock()
    private val databaseHelper: DatabaseHelper = mock()
    private val configurationHolder: ReportComponentConfigurationHolder = mock()
    private val componentId: ComponentId = mock()
    private val commonEventsConditions = listOf(mock<EventCondition>(), mock<EventCondition>())
    private val forceSendEventsConditions = listOf(mock<EventCondition>(), mock<EventCondition>())

    @get:Rule
    val componentEventConditionsProviderConstructionRule = constructionRule<ComponentEventConditionsProvider> {
        on { getCommonEventConditions() } doReturn commonEventsConditions
        on { getForceSendEventConditions() } doReturn forceSendEventsConditions
    }

    @get:Rule
    val conditionEventTriggerConstructionRule = constructionRule<ConditionalEventTrigger>()

    private val componentEventTriggerProvider: ComponentEventTriggerProvider by setUp {
        ComponentEventTriggerProvider(eventFlusher, databaseHelper, configurationHolder, componentId)
    }

    @Test
    fun eventTrigger() {
        assertThat(componentEventTriggerProvider.eventTrigger)
            .isEqualTo(conditionEventTriggerConstructionRule.constructionMock.constructed().first())
        assertThat(conditionEventTriggerConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(conditionEventTriggerConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(eventFlusher, commonEventsConditions, forceSendEventsConditions, componentId)
    }
}
